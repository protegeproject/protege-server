package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;

import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

public class ChangeHistoryImpl implements ChangeHistory {

    public static Logger logger = LoggerFactory.getLogger(ChangeHistoryImpl.class);

    private DocumentRevision startRevision;
    private DocumentRevision headRevision;
    private SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions = new TreeMap<>();
    private SortedMap<DocumentRevision, ChangeMetadata> logs = new TreeMap<>();

    public ChangeHistoryImpl() {
        this.startRevision = DocumentRevision.START_REVISION;
        this.headRevision = DocumentRevision.START_REVISION;
    }

    private ChangeHistoryImpl(@Nonnull DocumentRevision startRevision,
            @Nonnull SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions,
            @Nonnull SortedMap<DocumentRevision, ChangeMetadata> logs) {
        this.startRevision = startRevision;
        this.revisions = revisions;
        this.logs = logs;
        headRevision = startRevision.next(revisions.size());
    }

    public static ChangeHistoryImpl createEmptyChangeHistory() {
        return new ChangeHistoryImpl();
    }

    public static ChangeHistoryImpl recreate(@Nonnull DocumentRevision startRevision,
            @Nonnull SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions,
            @Nonnull SortedMap<DocumentRevision, ChangeMetadata> logs) {
        return new ChangeHistoryImpl(startRevision, revisions, logs);
    }

    @Override
    public void addRevisionBundle(ChangeMetadata metadata, List<OWLOntologyChange> changes) {
        DocumentRevision nextRevision = headRevision.next();
        logs.put(nextRevision, metadata);
        revisions.put(nextRevision, changes);
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
        return logs.get(revision);
    }

    @Override
    public SortedMap<DocumentRevision, List<OWLOntologyChange>> getRevisions() {
        return revisions;
    }

    @Override
    public SortedMap<DocumentRevision, ChangeMetadata> getRevisionLogs() {
        return logs;
    }

    @Override
    public boolean isEmpty() {
        return revisions.isEmpty();
    }

    @Override
    public int hashCode() {
        return getStartRevision().hashCode() + getHeadRevision().hashCode() + 42 * getRevisions().hashCode()
                + getRevisionLogs().hashCode() / 42;
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
                && other.getRevisions().equals(getRevisions())
                && other.getRevisionLogs().equals(getRevisionLogs());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(startRevision);
        sb.append(" --> ");
        sb.append(getHeadRevision());
        sb.append(": ");
        for (List<OWLOntologyChange> changesAtRevision : revisions.values()) {
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
