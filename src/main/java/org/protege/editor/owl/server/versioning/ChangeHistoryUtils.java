package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;

import org.semanticweb.binaryowl.BinaryOWLChangeLogHandler;
import org.semanticweb.binaryowl.BinaryOWLMetadata;
import org.semanticweb.binaryowl.BinaryOWLOntologyChangeLog;
import org.semanticweb.binaryowl.change.OntologyChangeRecordList;
import org.semanticweb.binaryowl.chunk.SkipSetting;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ChangeHistoryUtils {

    /**
     * Returns a change history that is a sub history of the input <code>changeHistory</code>. The crop begins
     * at the specified <code>start + 1</code> revision and extends to the to the <code>end</code> revision.
     *
     * @param changeHistory
     *          The input change history.
     * @param start
     *          The beginning revision, exclusive
     * @param end
     *          The ending revision, inclusive
     * @return The specified sub history.
     */
    public static ChangeHistory crop(@Nonnull ChangeHistory changeHistory, @Nonnull DocumentRevision start, @Nonnull DocumentRevision end) {
        if (start.behind(changeHistory.getBaseRevision())) {
            throw new IllegalArgumentException("The input start is out of the range");
        }
        if (end.aheadOf(changeHistory.getHeadRevision())) {
            throw new IllegalArgumentException("The input start is out of the range");
        }
        SortedMap<DocumentRevision, List<OWLOntologyChange>> subRevisions = new TreeMap<>();
        SortedMap<DocumentRevision, RevisionMetadata> subMetadata = new TreeMap<>();
        if (start.sameAs(changeHistory.getBaseRevision()) && end.sameAs(changeHistory.getHeadRevision())) {
            subRevisions.putAll(changeHistory.getRevisions());
            subMetadata.putAll(changeHistory.getMetadata());
        }
        else {
            subRevisions.putAll(changeHistory.getRevisions().headMap(start.next()).tailMap(end));
            subMetadata.putAll(changeHistory.getMetadata().headMap(start.next()).tailMap(end));
        }
        return ChangeHistoryImpl.recreate(start, subRevisions, subMetadata);
    }

    /**
     * Returns a change history that is a sub history of the input <code>changeHistory</code>. The crop begins
     * at the specified <code>start + 1</code> revision and extends to the to end of the change history.
     *
     * @param changeHistory
     *          The input change history.
     * @param start
     *          The beginning revision, exclusive
     * @return The specified sub history.
     */
    public static ChangeHistory crop(@Nonnull ChangeHistory changeHistory, @Nonnull DocumentRevision start) {
        return crop(changeHistory, start, changeHistory.getHeadRevision());
    }

    /**
     * Returns a change history that is a sub history of the input <code>changeHistory</code>. The crop begins
     * at the specified <code>start + 1</code> revision and extends to the length of the <code>offset</code> size.
     *
     * @param changeHistory
     *          The input change history.
     * @param start
     *          The beginning revision, exclusive
     * @param offset
     *          Length of the sub history
     * @return The specified sub history.
     */
    public static ChangeHistory crop(@Nonnull ChangeHistory changeHistory, @Nonnull DocumentRevision start, int offset) {
        return crop(changeHistory, start, start.next(offset));
    }

    /**
     * Writes an empty change history to the specified input <code>historyFile</code>
     *
     * @param historyFile
     *          The destination file. It must already exist in the file system.
     * @throws IOException
     */
    public static void writeEmptyChanges(@Nonnull HistoryFile historyFile) throws IOException {
        writeChanges(ChangeHistoryImpl.createEmptyChangeHistory(), historyFile);
    }

    /**
     * Writes the given <code>changeHistory</code> to the specified input <code>historyFile</code>.
     *
     * @param changeHistory
     *          The input change history
     * @param historyFile
     *          The destination file. It must already exist in the file system.
     * @throws IOException
     */
    public static void writeChanges(@Nonnull ChangeHistory changeHistory, @Nonnull HistoryFile historyFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(historyFile)));
        try {
            oos.writeObject(changeHistory.getBaseRevision());
            oos.writeObject(changeHistory.getMetadata());

            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            for (List<OWLOntologyChange> changeSet : changeHistory.getRevisions().values()) {
                log.appendChanges(changeSet, System.currentTimeMillis(), BinaryOWLMetadata.emptyMetadata(), oos);
            }
        }
        finally {
            oos.flush();
            oos.close();
        }
    }

    /**
     * Reads change history from the input <code>historyFile</code>. The method returns a change history that begins
     * at the specified <code>start + 1</code> revision and extends to the to the <code>end</code> revision.
     *
     * @param historyFile
     *          The input history file
     * @param start
     *          The beginning revision, exclusive
     * @param end
     *          The ending revision, inclusive
     * @return The specified sub history
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ChangeHistory readChanges(@Nonnull HistoryFile historyFile, @Nonnull DocumentRevision start,
            @Nonnull DocumentRevision end) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(historyFile)));
        try {
            DocumentRevision baseRevision = getBaseRevision(ois); // Start revision from the input history file
            if (start.behind(baseRevision)) {
                throw new IllegalArgumentException("Changes could not be extracted because the input start revision is out of range");
            }
            SortedMap<DocumentRevision, RevisionMetadata> metadata = getRevisionMetadataFromInputStream(ois);
            SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions = getRevisionsFromInputStream(ois, baseRevision);
            SortedMap<DocumentRevision, List<OWLOntologyChange>> subRevisions = revisions.tailMap(start.next()).headMap(end);
            SortedMap<DocumentRevision, RevisionMetadata> subMetadata = metadata.tailMap(start.next()).headMap(end);
            return ChangeHistoryImpl.recreate(start, subRevisions, subMetadata);
        }
        finally {
            ois.close();
        }
    }

    /**
     * Reads the whole change history from the input <code>historyFile</code>.
     *
     * @param historyFile
     *          The input history file.
     * @return A complete change history from the input history file.
     * @throws IOException
     */
    public static ChangeHistory readChanges(@Nonnull HistoryFile historyFile) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(historyFile)));
        try {
            DocumentRevision baseRevision = getBaseRevision(ois); // Start revision from the input history file
            SortedMap<DocumentRevision, RevisionMetadata> metadata = getRevisionMetadataFromInputStream(ois);
            SortedMap<DocumentRevision, List<OWLOntologyChange>> revisionsList = getRevisionsFromInputStream(ois, baseRevision);
            return ChangeHistoryImpl.recreate(baseRevision, revisionsList, metadata);
        }
        finally {
            ois.close();
        }
    }

    /**
     * Extracts the ontology changes from the input <code>changeHistory</code> where these changes
     * are encoded to be applicable to the <code>targetOntology</code>.
     *
     * @param changeHistory
     *          The input change history.
     * @param targetOntology
     *          The ontology where the changes can be applied to.
     * @return A list of {@link OWLOntologyChange} instances.
     */
    public static List<OWLOntologyChange> getOntologyChanges(@Nonnull ChangeHistory changeHistory,
            @Nonnull OWLOntology targetOntology) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (List<OWLOntologyChange> change : changeHistory.getRevisions().values()) {
            changes.addAll(change);
        }
        return ReplaceChangedOntologyVisitor.mutate(targetOntology, normalizeChangeDelta(changes));
    }

    /**
     * Extracts the ontology changes from the input <code>changeHistory</code> that begins
     * at the specified <code>start + 1</code> revision and extends to the to the <code>end</code> revision.
     * These changes are encoded to be applicable to the given <code>targetOntology</code>
     *
     * @param changeHistory
     *          The input change history
     * @param start
     *          The beginning revision, exclusive
     * @param end
     *          The ending revision, inclusive
     * @param targetOntology
     *          The ontology where the changes can be applied to.
     * @return A list of {@link OWLOntologyChange} instances.
     */
    public static List<OWLOntologyChange> getOntologyChanges(@Nonnull ChangeHistory changeHistory,
            @Nonnull DocumentRevision start, @Nonnull DocumentRevision end, OWLOntology targetOntology) {
        ChangeHistory subChangeHistory = crop(changeHistory, start, end);
        return getOntologyChanges(subChangeHistory, targetOntology);
    }

    /**
     * Extracts the ontology changes from the input <code>changeHistory</code> that begins
     * at the specified <code>start + 1</code> revision and extends to the to end of the change history.
     * These changes are encoded to be applicable to the given <code>targetOntology</code>
     *
     * @param changeHistory
     *          The input change history
     * @param start
     *          The beginning revision, exclusive
     * @param targetOntology
     *          The ontology where the changes can be applied to.
     * @return A list of {@link OWLOntologyChange} instances.
     */
    public static List<OWLOntologyChange> getOntologyChanges(@Nonnull ChangeHistory changeHistory,
            @Nonnull DocumentRevision start, OWLOntology targetOntology) {
        ChangeHistory subChangeHistory = crop(changeHistory, start);
        return getOntologyChanges(subChangeHistory, targetOntology);
    }

    /**
     * Extracts the ontology changes from the input <code>changeHistory</code> that begins
     * at the specified <code>start + 1</code> revision and extends to the length of <code>offset</code>
     * size. These changes are encoded to be applicable to the given <code>targetOntology</code>
     *
     * @param changeHistory
     *          The input change history
     * @param start
     *          The beginning revision, exclusive
     * @param offset
     *          Length of the sub history
     * @param targetOntology
     *          The ontology where the changes can be applied to.
     * @return A list of {@link OWLOntologyChange} instances.
     */
    public static List<OWLOntologyChange> getOntologyChanges(@Nonnull ChangeHistory changeHistory,
            @Nonnull DocumentRevision start, int offset, OWLOntology targetOntology) {
        ChangeHistory subChangeHistory = crop(changeHistory, start, offset);
        return getOntologyChanges(subChangeHistory, targetOntology);
    }

    /*
     * Private helper methods
     */

    private static DocumentRevision getBaseRevision(ObjectInputStream ois) throws IOException {
        try {
            return (DocumentRevision) ois.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Failed to deserialize DocumentRevision from the input stream", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static SortedMap<DocumentRevision, RevisionMetadata> getRevisionMetadataFromInputStream(ObjectInputStream ois)
            throws IOException {
        try {
            return (SortedMap<DocumentRevision, RevisionMetadata>) ois.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Failed to deserialize ChangeMetadata from the input stream", e);
        }
    }

    private static SortedMap<DocumentRevision, List<OWLOntologyChange>> getRevisionsFromInputStream(ObjectInputStream ois,
            DocumentRevision baseRevision) throws IOException {
        SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions = new TreeMap<>();
        try {
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
            OWLOntology placeholder = owlManager.createOntology();
            log.readChanges(ois, owlManager.getOWLDataFactory(), new BinaryOWLChangeLogHandler() {
                private DocumentRevision nextRevision = baseRevision.next();
                @Override
                public void handleChangesRead(OntologyChangeRecordList list, SkipSetting skipSetting, long filePosition) {
                    List<OWLOntologyChangeRecord> changeRecords = list.getChangeRecords();
                    List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
                    for (OWLOntologyChangeRecord cr : changeRecords) {
                        OWLOntologyChangeData changeData = cr.getData();
                        OWLOntologyChange change = changeData.createOntologyChange(placeholder);
                        changes.add(change);
                    }
                    revisions.put(nextRevision, changes);
                    nextRevision = nextRevision.next();
                }
            });
        }
        catch (OWLOntologyCreationException e) {
            throw new IOException("Internal error while computing changes", e);
        }
        return revisions;
    }

    private static List<OWLOntologyChange> normalizeChangeDelta(List<OWLOntologyChange> revision) {
        CollectingChangeVisitor visitor = CollectingChangeVisitor.collectChanges(revision);
        List<OWLOntologyChange> normalizedChanges = new ArrayList<OWLOntologyChange>();
        if (visitor.getLastOntologyIDChange() != null) {
            normalizedChanges.add(visitor.getLastOntologyIDChange());
        }
        normalizedChanges.addAll(visitor.getLastImportChangeMap().values());
        normalizedChanges.addAll(visitor.getLastOntologyAnnotationChangeMap().values());
        normalizedChanges.addAll(visitor.getLastAxiomChangeMap().values());
        return normalizedChanges;
    }
}
