package org.protege.owl.server.changes;

import org.protege.owl.server.changes.api.ChangeHistory;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

public class ChangeHistoryImpl implements ChangeHistory, Serializable {

    private static final long serialVersionUID = -3842895051205436375L;

    public static Logger logger = LoggerFactory.getLogger(ChangeHistoryImpl.class);

    private OntologyDocumentRevision startRevision;
    private List<List<OWLOntologyChange>> revisionsList = new ArrayList<>();
    private SortedMap<OntologyDocumentRevision, ChangeMetadata> metadataMap = new TreeMap<>();

    public ChangeHistoryImpl() {
        this.startRevision = OntologyDocumentRevision.START_REVISION;
    }

    /* package */ ChangeHistoryImpl(@Nonnull OntologyDocumentRevision startRevision,
            @Nonnull List<List<OWLOntologyChange>> revisionsList,
            @Nonnull SortedMap<OntologyDocumentRevision, ChangeMetadata> metaDataMap) {
        this.startRevision = startRevision;
        this.revisionsList = revisionsList;
        this.metadataMap = metaDataMap;
    }

    public static ChangeHistoryImpl createEmptyChangeHistory() {
        return new ChangeHistoryImpl();
    }

    public void addRevisionBundle(OntologyDocumentRevision revision, ChangeMetadata metadata, List<OWLOntologyChange> changes) {
        metadataMap.put(revision, metadata);
        revisionsList.add(changes);
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
    public ChangeMetadata getChangeMetadataForRevision(OntologyDocumentRevision revision) {
        return metadataMap.get(revision);
    }

    @Override
    public List<List<OWLOntologyChange>> getRevisionsList() {
        return revisionsList;
    }

    @Override
    public Map<OntologyDocumentRevision, ChangeMetadata> getMetadataMap() {
        return metadataMap;
    }

    @Override
    public boolean isEmpty() {
        return revisionsList.isEmpty();
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
                start.getRevisionDifferenceFrom(startRevision),
                end.getRevisionDifferenceFrom(startRevision));
        SortedMap<OntologyDocumentRevision, ChangeMetadata> subMetaDataMap = cropMap(metadataMap, start, end);
        return new ChangeHistoryImpl(start, subChanges, subMetaDataMap);
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
    public ChangeHistory appendChanges(ChangeHistory additionalChangeHistory) {
        if (additionalChangeHistory.getEndRevision().compareTo(getEndRevision()) <= 0) {
            return this;
        }
        if (additionalChangeHistory.getStartRevision().compareTo(getEndRevision()) > 0) {
            throw new IllegalArgumentException("Changes could not be merged because there was a gap in the change history");
        }
        try {
            OWLOntology emptyOntology = OWLManager.createOWLOntologyManager().createOntology();
            ChangeHistoryImpl changeHistory = new ChangeHistoryImpl(startRevision,
                    new ArrayList<List<OWLOntologyChange>>(revisionsList),
                    new TreeMap<OntologyDocumentRevision, ChangeMetadata>(metadataMap));
            OntologyDocumentRevision currentRevision = changeHistory.getEndRevision();
            for (; additionalChangeHistory.getEndRevision().compareTo(currentRevision) > 0; currentRevision = currentRevision.next()) {
                ChangeMetadata metadata = additionalChangeHistory.getChangeMetadataForRevision(currentRevision);
                List<OWLOntologyChange> changes = additionalChangeHistory.cropChanges(currentRevision, currentRevision.next()).getChanges(emptyOntology);
                changeHistory.addRevisionBundle(currentRevision, metadata, changes);
            }
            return changeHistory;
        }
        catch (OWLOntologyCreationException e) {
            throw new IllegalStateException("Could not create an empty ontology");
        }
    }

    @Override
    public List<OWLOntologyChange> getChanges(OWLOntology sourceOntology) {
        List<OWLOntologyChange> filteredChanges = new ArrayList<OWLOntologyChange>();
        OntologyDocumentRevision revision = startRevision;
        for (List<OWLOntologyChange> change : revisionsList) {
            filteredChanges.addAll(change);
            revision = revision.next();
        }
        return ReplaceChangedOntologyVisitor.mutate(sourceOntology, normalizeChangeDelta(filteredChanges));
    }

    private List<OWLOntologyChange> normalizeChangeDelta(List<OWLOntologyChange> revision) {
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

    @Override
    public int hashCode() {
        try {
            OWLOntology emptyOntology = OWLManager.createOWLOntologyManager().createOntology();
            int hashCode = 314159 * getStartRevision().hashCode() + 271828 * getEndRevision().hashCode();
            OntologyDocumentRevision currentRevision = getStartRevision();
            for (; currentRevision.compareTo(getEndRevision()) < 0; currentRevision = currentRevision.next()) {
                hashCode = 42 * hashCode + getChangeMetadataForRevision(currentRevision).hashCode();
                hashCode = hashCode - cropChanges(currentRevision, currentRevision.next()).getChanges(emptyOntology).hashCode();
            }
            return hashCode;
        }
        catch (OWLOntologyCreationException e) {
            throw new IllegalStateException("Could not create an empty ontology");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChangeHistory)) {
            return false;
        }
        ChangeHistory other = (ChangeHistory) o;
        try {
            OWLOntology emptyOntology = OWLManager.createOWLOntologyManager().createOntology();
            if (!(getStartRevision().equals(other.getStartRevision())
                    && getEndRevision().equals(other.getEndRevision()))) {
                return false;
            }
            OntologyDocumentRevision currentRevision = getStartRevision();
            for (; currentRevision.compareTo(getEndRevision()) < 0; currentRevision = currentRevision.next()) {
                if (!(getChangeMetadataForRevision(currentRevision).equals(other.getChangeMetadataForRevision(currentRevision)))) {
                    return false;
                }
                if (!cropChanges(currentRevision, currentRevision.next()).getChanges(emptyOntology)
                        .equals(other.cropChanges(currentRevision, currentRevision.next()).getChanges(emptyOntology))) {
                    return false;
                }
            }
            return true;
        }
        catch (OWLOntologyCreationException e) {
            throw new IllegalStateException("Could not create an empty ontology");
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
