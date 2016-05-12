package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

public class ChangeHistoryImpl implements ChangeHistory, Serializable {

    private static final long serialVersionUID = -3842895051205436375L;

    public static Logger logger = LoggerFactory.getLogger(ChangeHistoryImpl.class);

    private DocumentRevision startRevision;
    private List<List<OWLOntologyChange>> revisionsList = new ArrayList<>();
    private SortedMap<DocumentRevision, ChangeMetadata> metadataMap = new TreeMap<>();

    public ChangeHistoryImpl() {
        this.startRevision = DocumentRevision.START_REVISION;
    }

    /* package */ ChangeHistoryImpl(@Nonnull DocumentRevision startRevision,
            @Nonnull List<List<OWLOntologyChange>> revisionsList,
            @Nonnull SortedMap<DocumentRevision, ChangeMetadata> metaDataMap) {
        this.startRevision = startRevision;
        this.revisionsList = revisionsList;
        this.metadataMap = metaDataMap;
    }

    public static ChangeHistoryImpl createEmptyChangeHistory() {
        return new ChangeHistoryImpl();
    }

    public void addRevisionBundle(DocumentRevision revision, ChangeMetadata metadata, List<OWLOntologyChange> changes) {
        metadataMap.put(revision, metadata);
        revisionsList.add(changes);
    }

    @Override
    public DocumentRevision getStartRevision() {
        return startRevision;
    }

    @Override
    public DocumentRevision getEndRevision() {
        return startRevision.next(revisionsList.size());
    }

    @Override
    public ChangeMetadata getChangeMetadataForRevision(DocumentRevision revision) {
        return metadataMap.get(revision);
    }

    @Override
    public List<List<OWLOntologyChange>> getRevisionsList() {
        return revisionsList;
    }

    @Override
    public SortedMap<DocumentRevision, ChangeMetadata> getMetadataMap() {
        return metadataMap;
    }

    @Override
    public boolean isEmpty() {
        return revisionsList.isEmpty();
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
                    new TreeMap<DocumentRevision, ChangeMetadata>(metadataMap));
            DocumentRevision currentRevision = changeHistory.getEndRevision();
            for (; additionalChangeHistory.getEndRevision().compareTo(currentRevision) > 0; currentRevision = currentRevision.next()) {
                ChangeMetadata metadata = additionalChangeHistory.getChangeMetadataForRevision(currentRevision);
                List<OWLOntologyChange> changes = ChangeHistoryUtils.crop(additionalChangeHistory, currentRevision, 1).getChanges(emptyOntology);
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
        DocumentRevision revision = startRevision;
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
        return getStartRevision().hashCode() + getEndRevision().hashCode() + 42 * getRevisionsList().hashCode()
                + getMetadataMap().hashCode() / 42;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChangeHistoryImpl)) {
            return false;
        }
        ChangeHistoryImpl other = (ChangeHistoryImpl) obj;
        return other.getStartRevision().equals(getStartRevision()) && other.getEndRevision().equals(getEndRevision())
                && other.getRevisionsList().equals(getRevisionsList())
                && other.getMetadataMap().equals(getMetadataMap());
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
