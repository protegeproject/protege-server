package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;

import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

public class ChangeHistoryImpl implements ChangeHistory {

    public static Logger logger = LoggerFactory.getLogger(ChangeHistoryImpl.class);

    private DocumentRevision startRevision;
    private DocumentRevision headRevision;
    private List<List<OWLOntologyChange>> revisionsList = new ArrayList<>();
    private SortedMap<DocumentRevision, ChangeMetadata> metadataMap = new TreeMap<>();

    public ChangeHistoryImpl() {
        this.startRevision = DocumentRevision.START_REVISION;
        this.headRevision = DocumentRevision.START_REVISION;
    }

    private ChangeHistoryImpl(@Nonnull DocumentRevision startRevision,
            @Nonnull List<List<OWLOntologyChange>> revisionsList,
            @Nonnull SortedMap<DocumentRevision, ChangeMetadata> metaDataMap) {
        this.startRevision = startRevision;
        this.revisionsList = revisionsList;
        this.metadataMap = metaDataMap;
        headRevision = startRevision.next(revisionsList.size());
    }

    public static ChangeHistoryImpl createEmptyChangeHistory() {
        return new ChangeHistoryImpl();
    }

    public static ChangeHistoryImpl recreate(@Nonnull DocumentRevision startRevision,
            @Nonnull List<List<OWLOntologyChange>> revisionsList,
            @Nonnull SortedMap<DocumentRevision, ChangeMetadata> changeMetadata) {
        return new ChangeHistoryImpl(startRevision, revisionsList, changeMetadata);
    }

    @Override
    public void addRevisionBundle(ChangeMetadata metadata, List<OWLOntologyChange> changes) {
        DocumentRevision nextRevision = headRevision.next();
        metadataMap.put(nextRevision, metadata);
        revisionsList.add(changes);
        headRevision = nextRevision;
    }

    @Override
    public DocumentRevision getStartRevision() {
        return startRevision;
    }

    @Override
    public DocumentRevision getHeadRevision() {
        return headRevision;
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
    public int hashCode() {
        return getStartRevision().hashCode() + getHeadRevision().hashCode() + 42 * getRevisionsList().hashCode()
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
        return other.getStartRevision().equals(getStartRevision()) && other.getHeadRevision().equals(getHeadRevision())
                && other.getRevisionsList().equals(getRevisionsList())
                && other.getMetadataMap().equals(getMetadataMap());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(startRevision);
        sb.append(" --> ");
        sb.append(getHeadRevision());
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
