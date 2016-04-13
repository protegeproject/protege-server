package org.protege.owl.server.changes;

import org.protege.owl.server.changes.api.ChangeHistory;
import org.protege.owl.server.changes.format.OWLOutputStream;
import org.protege.owl.server.util.ChangeUtilities;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

public class ChangeHistoryImpl implements ChangeHistory, Serializable {

    private static final long serialVersionUID = -3842895051205436375L;

    public static Logger logger = LoggerFactory.getLogger(ChangeHistoryImpl.class);

    private transient int compressionLimit = -1;
    private OntologyDocumentRevision startRevision;
    private List<List<OWLOntologyChange>> revisionsList = new ArrayList<>();
    private SortedMap<OntologyDocumentRevision, ChangeMetaData> metadataMap = new TreeMap<>();

    public ChangeHistoryImpl(@Nonnull OntologyDocumentRevision startRevision,
            @Nonnull List<OWLOntologyChange> changes, @Nonnull ChangeMetaData metadata) {
        this.startRevision = startRevision;
        this.revisionsList.add(new ArrayList<OWLOntologyChange>(changes));
        this.metadataMap.put(startRevision, metadata);
    }

    /* Utility constructors */
    /* package */ ChangeHistoryImpl(@Nonnull OntologyDocumentRevision startRevision) {
        this.startRevision = startRevision;
    }

    /* package */ ChangeHistoryImpl(OntologyDocumentRevision startRevision,
            List<List<OWLOntologyChange>> revisionsList,
            SortedMap<OntologyDocumentRevision, ChangeMetaData> metaDataMap) {
        this.startRevision = startRevision;
        this.revisionsList = revisionsList;
        this.metadataMap = metaDataMap;
    }

    public static ChangeHistoryImpl createEmptyChangeHistory() {
        return new ChangeHistoryImpl(OntologyDocumentRevision.START_REVISION);
    }

    @Override
    public void setCompressionLimit(int compressionLimit) {
        this.compressionLimit = compressionLimit;
    }

    @Override
    public OntologyDocumentRevision getStartRevision() {
        return startRevision;
    }

    @Override
    public OntologyDocumentRevision getEndRevision() {
        return startRevision.add(revisionsList.size());
    }

    @Override
    public ChangeMetaData getMetaData(OntologyDocumentRevision revision) {
        return metadataMap.get(revision);
    }

    @Override
    public ChangeHistory cropChanges(OntologyDocumentRevision start, OntologyDocumentRevision end) {
        if (start == null || start.compareTo(getStartRevision()) < 0) {
            start = getStartRevision();
        }
        if (end == null || end.compareTo(getEndRevision()) > 0) {
            end = getEndRevision();
        }
        if (start.equals(getStartRevision()) && end.equals(getEndRevision())) {
            return this;
        }
        List<List<OWLOntologyChange>> subChanges = revisionsList.subList(
                start.getRevisionDifferenceFrom(startRevision), end.getRevisionDifferenceFrom(startRevision));
        SortedMap<OntologyDocumentRevision, ChangeMetaData> subMetaDataMap = cropMap(metadataMap, start, end);
        return new ChangeHistoryImpl(start, subChanges, subMetaDataMap);
    }

    @Override
    public boolean isEmpty() {
        return revisionsList.isEmpty();
    }

    private <X extends Comparable<X>, Y> SortedMap<X, Y> cropMap(SortedMap<X, Y> map, X start, X end) {
        if (map.isEmpty()) {
            return new TreeMap<X, Y>();
        }
        if (start.compareTo(map.lastKey()) > 0) {
            return new TreeMap<X, Y>();
        }
        if (end.compareTo(map.firstKey()) < 0) {
            return new TreeMap<X, Y>();
        }
        return map.tailMap(start).headMap(end);
    }

    @Override
    public ChangeHistory appendChanges(ChangeHistory additionalChanges) {
        if (additionalChanges.getEndRevision().compareTo(getEndRevision()) <= 0) {
            return this;
        }
        if (additionalChanges.getStartRevision().compareTo(getEndRevision()) > 0) {
            throw new IllegalArgumentException(
                    "Changes could not be merged because there was a gap in the change histories");
        }
        OWLOntology fakeOntology;
        try {
            fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
        }
        catch (OWLOntologyCreationException e) {
            throw new RuntimeException("This really shouldn't happen!", e);
        }
        ChangeHistoryImpl newDoc = new ChangeHistoryImpl(startRevision,
                new ArrayList<List<OWLOntologyChange>>(revisionsList),
                new TreeMap<OntologyDocumentRevision, ChangeMetaData>(metadataMap));
        OntologyDocumentRevision revision = newDoc.getEndRevision();
        for (; additionalChanges.getEndRevision().compareTo(revision) > 0; revision = revision.next()) {
            newDoc.metadataMap.put(revision, additionalChanges.getMetaData(revision));
            newDoc.revisionsList.add(
                    additionalChanges.cropChanges(revision, revision.next()).getChanges(fakeOntology));
        }
        return newDoc;
    }

    @Override
    public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
        List<OWLOntologyChange> filteredChanges = new ArrayList<OWLOntologyChange>();
        OntologyDocumentRevision revision = startRevision;
        for (List<OWLOntologyChange> change : revisionsList) {
            filteredChanges.addAll(change);
            revision = revision.next();
        }
        return ReplaceChangedOntologyVisitor.mutate(ontology, ChangeUtilities.normalizeChangeDelta(filteredChanges));
    }

    @Override
    public void writeChangeDocument(OutputStream out) throws IOException {
        long startTime = System.currentTimeMillis();
        ObjectOutputStream oos;
        if (out instanceof ObjectOutputStream) {
            oos = (ObjectOutputStream) out;
        }
        else {
            oos = new ObjectOutputStream(out);
        }
        oos.writeObject(startRevision);
        oos.writeObject(metadataMap);
        oos.writeInt(revisionsList.size());
        OWLOutputStream owlstream = new OWLOutputStream(oos);
        owlstream.setCompressionLimit(compressionLimit);
        for (List<OWLOntologyChange> changeSet : revisionsList) {
            owlstream.writeWithCompression(changeSet);
        }
        oos.flush();
        logLongWrite(System.currentTimeMillis() - startTime);
    }

    private void logLongWrite(long interval) {
        if (interval > 1000) {
            int totalChanges = 0;
            for (List<OWLOntologyChange> changeList : revisionsList) {
                totalChanges += changeList.size();
            }
            String template = "Write of change history ({} changes) took {} seconds (compression limit = {}).";
            logger.info(template, totalChanges, interval/1000, compressionLimit);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeChangeDocument(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // TODO Implement later
//        documentFactory = (DocumentFactory) in.readObject();
//        ChangeHistoryImpl doc = (ChangeHistoryImpl) documentFactory.readChangeDocument(in, null, null);
//        startRevision = doc.getStartRevision();
//        listOfRevisionChanges = doc.listOfRevisionChanges;
//        metadataMap = doc.metadataMap;
    }

    @Override
    public int hashCode() {
        try {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
            int hashCode = 314159 * getStartRevision().hashCode() + 271828 * getEndRevision().hashCode();
            for (OntologyDocumentRevision revision = getStartRevision(); revision
                    .compareTo(getEndRevision()) < 0; revision = revision.next()) {
                hashCode = 42 * hashCode + getMetaData(revision).hashCode();
                hashCode = hashCode - cropChanges(revision, revision.next()).getChanges(ontology).hashCode();
            }
            return hashCode;
        }
        catch (OWLOntologyCreationException e) {
            throw new IllegalStateException("Could not create empty ontology");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChangeHistory)) {
            return false;
        }
        ChangeHistory other = (ChangeHistory) o;
        try {
            OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
            if (!(getStartRevision().equals(other.getStartRevision()) 
                    && getEndRevision().equals(other.getEndRevision()))) {
                return false;
            }
            for (OntologyDocumentRevision revision = getStartRevision(); revision
                    .compareTo(getEndRevision()) < 0; revision = revision.next()) {
                if (!(getMetaData(revision).equals(other.getMetaData(revision)))) {
                    return false;
                }
                if (!cropChanges(revision, revision.next()).getChanges(ontology)
                        .equals(other.cropChanges(revision, revision.next()).getChanges(ontology))) {
                    return false;
                }
            }
            return true;
        }
        catch (OWLOntologyCreationException e) {
            throw new IllegalStateException("Could not create empty ontology");
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(startRevision);
        sb.append(" --> ");
        sb.append(getEndRevision());
        sb.append(": ");
        for (List<OWLOntologyChange> changesAtRevision : revisionsList) {
            sb.append("[");
            boolean needComma = false;
            for (OWLOntologyChange particularChangeAtRevision : changesAtRevision) {
                if (needComma) {
                    sb.append(", ");
                }
                sb.append(particularChangeAtRevision);
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }
}
