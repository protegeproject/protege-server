package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;

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

public class ChangeHistoryUtils {

    public static ChangeHistory crop(@Nonnull ChangeHistory changeHistory, @Nonnull DocumentRevision start, @Nonnull DocumentRevision end) {
        if (start.behind(changeHistory.getStartRevision())) {
            throw new IllegalArgumentException("The input start is out of the range");
        }
        if (end.aheadOf(changeHistory.getHeadRevision())) {
            throw new IllegalArgumentException("The input start is out of the range");
        }
        List<List<OWLOntologyChange>> subRevisions = new ArrayList<>();
        SortedMap<DocumentRevision, ChangeMetadata> subMetadata = new TreeMap<>();
        if (start.sameAs(changeHistory.getStartRevision()) && end.sameAs(changeHistory.getHeadRevision())) {
            subRevisions.addAll(changeHistory.getRevisionsList());
            subMetadata.putAll(changeHistory.getMetadataMap());
        }
        else {
            subRevisions.addAll(changeHistory.getRevisionsList().subList(
                    DocumentRevision.distance(start, changeHistory.getStartRevision()),
                    DocumentRevision.distance(end, changeHistory.getStartRevision())));
            subMetadata.putAll(changeHistory.getMetadataMap().headMap(start).tailMap(end));
        }
        return new ChangeHistoryImpl(start, subRevisions, subMetadata);
    }

    public static ChangeHistory crop(@Nonnull ChangeHistory changeHistory, @Nonnull DocumentRevision start) {
        return crop(changeHistory, start, changeHistory.getHeadRevision());
    }

    public static ChangeHistory crop(@Nonnull ChangeHistory changeHistory, @Nonnull DocumentRevision start, int offset) {
        return crop(changeHistory, start, start.next(offset));
    }

    public static void writeEmptyChanges(@Nonnull HistoryFile historyFile) throws IOException {
        writeChanges(ChangeHistoryImpl.createEmptyChangeHistory(), historyFile);
    }

    public static void writeChanges(@Nonnull ChangeHistory changeHistory, @Nonnull HistoryFile historyFile) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(historyFile)));
        try {
            oos.writeObject(changeHistory.getStartRevision());
            oos.writeObject(changeHistory.getMetadataMap());

            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            for (List<OWLOntologyChange> changeSet : changeHistory.getRevisionsList()) {
                log.appendChanges(changeSet, System.currentTimeMillis(), BinaryOWLMetadata.emptyMetadata(), oos);
            }
        }
        finally {
            oos.flush();
            oos.close();
        }
    }

    public static ChangeHistory readChanges(@Nonnull HistoryFile historyFile, @Nonnull DocumentRevision startRevision,
            @Nonnull DocumentRevision endRevision) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(historyFile)));
        try {
            DocumentRevision baseStartRevision = getBaseStartRevision(ois); // Start revision from the input history file
            if (startRevision.behind(baseStartRevision)) {
                throw new IllegalArgumentException("Changes could not be extracted because the input start revision is out of range");
            }
            SortedMap<DocumentRevision, ChangeMetadata> metadata = getMetadataMap(ois);
            List<List<OWLOntologyChange>> revisionsList = getRevisionsList(ois);
            List<List<OWLOntologyChange>> subChanges = revisionsList.subList(
                    DocumentRevision.distance(startRevision, baseStartRevision),
                    DocumentRevision.distance(endRevision, baseStartRevision));
            SortedMap<DocumentRevision, ChangeMetadata> subMetadataMap = metadata.tailMap(startRevision).headMap(endRevision);
            return new ChangeHistoryImpl(startRevision, subChanges, subMetadataMap);
        }
        finally {
            ois.close();
        }
    }

    public static ChangeHistory readChanges(@Nonnull HistoryFile historyFile) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(historyFile)));
        try {
            DocumentRevision startRevision = getBaseStartRevision(ois); // Start revision from the input history file
            SortedMap<DocumentRevision, ChangeMetadata> metadata = getMetadataMap(ois);
            List<List<OWLOntologyChange>> revisionsList = getRevisionsList(ois);
            return new ChangeHistoryImpl(startRevision, revisionsList, metadata);
        }
        finally {
            ois.close();
        }
    }

    /*
     * Private helper methods
     */

    private static DocumentRevision getBaseStartRevision(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return (DocumentRevision) ois.readObject();
    }

    @SuppressWarnings("unchecked")
    private static SortedMap<DocumentRevision, ChangeMetadata> getMetadataMap(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return (SortedMap<DocumentRevision, ChangeMetadata>) ois.readObject();
    }

    private static List<List<OWLOntologyChange>> getRevisionsList(ObjectInputStream ois) throws IOException {
        List<List<OWLOntologyChange>> revisionsList = new ArrayList<List<OWLOntologyChange>>();
        try {
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
            OWLOntology placeholder = owlManager.createOntology();
            log.readChanges(ois, owlManager.getOWLDataFactory(), new BinaryOWLChangeLogHandler() {
                @Override
                public void handleChangesRead(OntologyChangeRecordList list, SkipSetting skipSetting, long filePosition) {
                    List<OWLOntologyChangeRecord> changeRecords = list.getChangeRecords();
                    List<OWLOntologyChange> revisions = new ArrayList<OWLOntologyChange>();
                    for (OWLOntologyChangeRecord cr : changeRecords) {
                        OWLOntologyChangeData changeData = cr.getData();
                        OWLOntologyChange change = changeData.createOntologyChange(placeholder);
                        revisions.add(change);
                    }
                    revisionsList.add(revisions);
                }
            });
        }
        catch (OWLOntologyCreationException e) {
            throw new IOException("Internal error while computing changes", e);
        }
        return revisionsList;
    }
}
