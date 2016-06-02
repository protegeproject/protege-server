package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;

import org.semanticweb.binaryowl.BinaryOWLMetadata;
import org.semanticweb.binaryowl.BinaryOWLOntologyChangeLog;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
            throw new IllegalArgumentException("The input end is out of the range");
        }
        if (start.equals(end)) {
            return ChangeHistoryImpl.createEmptyChangeHistory(start);
        }
        else {
            SortedMap<DocumentRevision, List<OWLOntologyChange>> subRevisions = new TreeMap<>();
            SortedMap<DocumentRevision, RevisionMetadata> subMetadata = new TreeMap<>();
            if (start.sameAs(changeHistory.getBaseRevision()) && end.sameAs(changeHistory.getHeadRevision())) {
                subRevisions.putAll(changeHistory.getRevisions());
                subMetadata.putAll(changeHistory.getMetadata());
            }
            else {
                subRevisions.putAll(changeHistory.getRevisions().tailMap(start.next()).headMap(end.next()));
                subMetadata.putAll(changeHistory.getMetadata().tailMap(start.next()).headMap(end.next()));
            }
            return ChangeHistoryImpl.recreate(start, subRevisions, subMetadata);
        }
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
     * Appends the given <code>changeHistory</code> to the specified input <code>historyFile</code>.
     *
     * @param changeHistory
     *          The input change history
     * @param historyFile
     *          The destination file. It must already exist in the file system.
     * @throws IOException
     */
    public static void appendChanges(@Nonnull ChangeHistory changeHistory, @Nonnull HistoryFile historyFile) throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(historyFile, true));
        try {
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            DocumentRevision base = changeHistory.getBaseRevision();
            DocumentRevision head = changeHistory.getHeadRevision();
            for (DocumentRevision current = base.next(); current.behindOrSameAs(head); current = current.next()) {
                List<OWLOntologyChange> changeSet = changeHistory.getChangesForRevision(current);
                RevisionMetadata metadata = changeHistory.getMetadataForRevision(current);
                BinaryOWLMetadata changeMetadata = getBinaryOWLMetadata(metadata);
                log.appendChanges(changeSet, current.getRevisionNumber(), changeMetadata, outputStream); // TODO Report API misuse, timestamp == revision number
            }
        }
        finally {
            outputStream.flush();
            outputStream.close();
        }
    }

    /**
     * Reads change history from the input <code>historyFile</code>. The method returns a change history that begins
     * at the specified <code>start</code> revision and extends to the to the <code>end</code> revision.
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
        final SortedMap<DocumentRevision, RevisionMetadata> metadata = new TreeMap<>();
        final SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions = new TreeMap<>();
        FileInputStream inputStream = new FileInputStream(historyFile);
        try {
            readBinaryOWLChangeLog(inputStream, metadata, revisions);
            SortedMap<DocumentRevision, List<OWLOntologyChange>> subRevisions = revisions.tailMap(start).headMap(end);
            SortedMap<DocumentRevision, RevisionMetadata> subMetadata = metadata.tailMap(start).headMap(end);
            DocumentRevision baseRevision = start.previous();
            return ChangeHistoryImpl.recreate(baseRevision, subRevisions, subMetadata);
        }
        finally {
            if (inputStream != null) {
                inputStream.close();
            }
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
        final SortedMap<DocumentRevision, RevisionMetadata> metadata = new TreeMap<>();
        final SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions = new TreeMap<>();
        FileInputStream inputStream = new FileInputStream(historyFile);
        try {
            readBinaryOWLChangeLog(inputStream, metadata, revisions);
            if (metadata.isEmpty() && revisions.isEmpty()) {
                return ChangeHistoryImpl.createEmptyChangeHistory();
            }
            else {
                DocumentRevision baseRevision = revisions.firstKey().previous();
                return ChangeHistoryImpl.recreate(baseRevision, revisions, metadata);
            }
        }
        finally {
            if (inputStream != null) {
                inputStream.close();
            }
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
        if (!changeHistory.isEmpty()) {
            for (List<OWLOntologyChange> change : changeHistory.getRevisions().values()) {
                changes.addAll(change);
            }
            return ReplaceChangedOntologyVisitor.mutate(targetOntology, normalizeChangeDelta(changes));
        }
        return changes;
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

    private static void readBinaryOWLChangeLog(FileInputStream inputStream,
            SortedMap<DocumentRevision, RevisionMetadata> resultMetadata,
            SortedMap<DocumentRevision, List<OWLOntologyChange>> resultRevisions) throws IOException {
        try {
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
            OWLOntology placeholder = owlManager.createOntology();
            log.readChanges(inputStream, owlManager.getOWLDataFactory(), (list, skipSetting, filePosition) -> {
                // Get the revision number
                int revision = (int) list.getTimestamp(); // TODO Report API misuse, timestamp == revision number
                
                // Get the metadata
                BinaryOWLMetadata metadataRecord = list.getMetadata();
                RevisionMetadata metadata = getRevisionMetadata(metadataRecord);
                resultMetadata.put(DocumentRevision.create(revision), metadata);
    
                // Get the changes 
                List<OWLOntologyChangeRecord> changeRecords = list.getChangeRecords();
                List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
                for (OWLOntologyChangeRecord cr : changeRecords) {
                    OWLOntologyChangeData changeData = cr.getData();
                    OWLOntologyChange change = changeData.createOntologyChange(placeholder);
                    changes.add(change);
                }
                resultRevisions.put(DocumentRevision.create(revision), changes);
            });
        }
        catch (OWLOntologyCreationException e) {
            throw new IOException("Internal error while computing changes", e);
        }
    }

    private static BinaryOWLMetadata getBinaryOWLMetadata(RevisionMetadata metadata) {
        BinaryOWLMetadata metadataRecord = new BinaryOWLMetadata();
        metadataRecord.setStringAttribute(RevisionMetadata.AUTHOR_USERNAME, metadata.getAuthorId());
        metadataRecord.setStringAttribute(RevisionMetadata.AUTHOR_NAME, metadata.getAuthorName());
        metadataRecord.setStringAttribute(RevisionMetadata.AUTHOR_EMAIL, metadata.getAuthorEmail());
        metadataRecord.setLongAttribute(RevisionMetadata.CHANGE_DATE, metadata.getDate().getTime());
        metadataRecord.setStringAttribute(RevisionMetadata.CHANGE_COMMENT, metadata.getComment());
        return metadataRecord;
    }

    private static RevisionMetadata getRevisionMetadata(BinaryOWLMetadata metadata) {
        String authorId = metadata.getStringAttribute(RevisionMetadata.AUTHOR_USERNAME, "");
        String authorName = metadata.getStringAttribute(RevisionMetadata.AUTHOR_NAME, "");
        String authorEmail = metadata.getStringAttribute(RevisionMetadata.AUTHOR_EMAIL, "");
        Date changeDate = new Date(metadata.getLongAttribute(RevisionMetadata.CHANGE_DATE, 0L));
        String comment = metadata.getStringAttribute(RevisionMetadata.CHANGE_COMMENT, "");
        return new RevisionMetadata(authorId, authorName, authorEmail, changeDate, comment);
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
