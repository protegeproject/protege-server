package org.protege.owl.server.versioning;

import org.protege.owl.server.versioning.api.ChangeHistory;
import org.protege.owl.server.versioning.format.OWLInputStream;
import org.protege.owl.server.versioning.format.OWLOutputStream;

import org.semanticweb.owlapi.model.OWLOntologyChange;

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

import javax.annotation.Nonnull;

public class ChangeHistoryUtilities {

    private static final int DEFAULT_COMPRESSION_LIMIT = -1;

    public static void writeEmptyChanges(@Nonnull HistoryFile historyFile) throws IOException {
        writeChanges(ChangeHistoryImpl.createEmptyChangeHistory(), historyFile, DEFAULT_COMPRESSION_LIMIT);
    }

    public static void writeChanges(@Nonnull ChangeHistory changeHistory, @Nonnull HistoryFile historyFile) throws IOException {
        writeChanges(changeHistory, historyFile, DEFAULT_COMPRESSION_LIMIT);
    }

    public static void writeChanges(@Nonnull ChangeHistory changeHistory, @Nonnull HistoryFile historyFile, int compressionLimit) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(historyFile)));
        OWLOutputStream owlos = new OWLOutputStream(oos);
        owlos.setCompressionLimit(compressionLimit);
        try {
            List<List<OWLOntologyChange>> revisionsList = changeHistory.getRevisionsList();
            oos.writeObject(changeHistory.getStartRevision());
            oos.writeObject(changeHistory.getMetadataMap());
            oos.writeInt(revisionsList.size());
            for (List<OWLOntologyChange> revision : revisionsList) {
                owlos.writeWithCompression(revision);
            }
        }
        finally {
            oos.flush();
            oos.close();
        }
    }

    public static ChangeHistory readChanges(@Nonnull HistoryFile historyFile, @Nonnull OntologyDocumentRevision startRevision,
            @Nonnull OntologyDocumentRevision endRevision) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(historyFile)));
        OWLInputStream owlis = new OWLInputStream(ois);
        try {
            OntologyDocumentRevision originalStartRevision = getStartRevision(ois);
            if (originalStartRevision.getRevisionDifferenceFrom(startRevision) > 0) {
                throw new IllegalArgumentException("Changes could not be extracted because the start revision is out of range");
            }
            SortedMap<OntologyDocumentRevision, ChangeMetadata> metadata = getMetadataMap(ois);
            int iteration = getRevisionSize(ois);
            
            List<List<OWLOntologyChange>> revisionsList = new ArrayList<List<OWLOntologyChange>>();
            OntologyDocumentRevision currentRevision = startRevision;
            while (iteration != 0) {
                List<OWLOntologyChange> revision = getRevision(owlis);
                if (currentRevision.getRevisionDifferenceFrom(startRevision) >= 0
                        && currentRevision.getRevisionDifferenceFrom(endRevision) < 0) {
                    revisionsList.add(revision);
                }
                currentRevision = currentRevision.next();
                iteration--;
            }
            endRevision = startRevision.add(revisionsList.size()); // re-adjust the end revision
            metadata = metadata.tailMap(startRevision).headMap(endRevision);
            return new ChangeHistoryImpl(startRevision, revisionsList, metadata);
        }
        finally {
            ois.close();
        }
    }

    public static ChangeHistory readChanges(@Nonnull HistoryFile historyFile) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(historyFile)));
        OWLInputStream owlis = new OWLInputStream(ois);
        try {
            SortedMap<OntologyDocumentRevision, ChangeMetadata> metadata = getMetadataMap(ois);
            int iteration = getRevisionSize(ois);
            
            List<List<OWLOntologyChange>> revisionsList = new ArrayList<List<OWLOntologyChange>>();
            OntologyDocumentRevision startRevision = OntologyDocumentRevision.START_REVISION;
            while (iteration != 0) {
                List<OWLOntologyChange> revision = getRevision(owlis);
                revisionsList.add(revision);
                iteration--;
            }
            OntologyDocumentRevision endRevision = startRevision.add(revisionsList.size());
            metadata = metadata.tailMap(startRevision).headMap(endRevision);
            return new ChangeHistoryImpl(startRevision, revisionsList, metadata);
        }
        finally {
            ois.close();
        }
    }

    private static OntologyDocumentRevision getStartRevision(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return (OntologyDocumentRevision) ois.readObject();
    }

    @SuppressWarnings("unchecked")
    private static SortedMap<OntologyDocumentRevision, ChangeMetadata> getMetadataMap(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return (SortedMap<OntologyDocumentRevision, ChangeMetadata>) ois.readObject();
    }

    private static int getRevisionSize(ObjectInputStream ois) throws IOException {
        return ois.readInt();
    }

    @SuppressWarnings("unchecked")
    private static List<OWLOntologyChange> getRevision(OWLInputStream owlis) throws IOException {
        return (List<OWLOntologyChange>) owlis.read();
    }
}
