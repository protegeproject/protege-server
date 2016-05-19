package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;

import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * @author Timothy Redmond (tredmond) <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ChangeHistoryImpl implements ChangeHistory {

    private static final long serialVersionUID = -8436246195642942303L;

    public static Logger logger = LoggerFactory.getLogger(ChangeHistoryImpl.class);

    private DocumentRevision baseRevision;
    private DocumentRevision headRevision;
    private SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions = new TreeMap<>();
    private SortedMap<DocumentRevision, RevisionMetadata> logs = new TreeMap<>();

    public ChangeHistoryImpl() {
        this.baseRevision = DocumentRevision.START_REVISION;
        this.headRevision = DocumentRevision.START_REVISION;
    }

    public ChangeHistoryImpl(@Nonnull DocumentRevision baseRevision) {
        this.baseRevision = baseRevision;
        this.headRevision = baseRevision;
    }

    private ChangeHistoryImpl(@Nonnull DocumentRevision startRevision,
            @Nonnull SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions,
            @Nonnull SortedMap<DocumentRevision, RevisionMetadata> logs) {
        this.baseRevision = startRevision;
        this.revisions = revisions;
        this.logs = logs;
        headRevision = startRevision.next(revisions.size());
    }

    public static ChangeHistoryImpl createEmptyChangeHistory() {
        return new ChangeHistoryImpl();
    }

    public static ChangeHistoryImpl createEmptyChangeHistory(DocumentRevision baseRevision) {
        return new ChangeHistoryImpl(baseRevision);
    }

    public static ChangeHistoryImpl createEmptyChangeHistory(int baseRevisionNumber) {
        return new ChangeHistoryImpl(DocumentRevision.create(baseRevisionNumber));
    }

    public static ChangeHistoryImpl recreate(@Nonnull DocumentRevision startRevision,
            @Nonnull SortedMap<DocumentRevision, List<OWLOntologyChange>> revisions,
            @Nonnull SortedMap<DocumentRevision, RevisionMetadata> logs) {
        return new ChangeHistoryImpl(startRevision, revisions, logs);
    }

    @Override
    public void addRevision(RevisionMetadata metadata, List<OWLOntologyChange> changes) {
        DocumentRevision nextRevision = headRevision.next();
        logs.put(nextRevision, metadata);
        revisions.put(nextRevision, changes);
        headRevision = nextRevision;
    }

    @Override
    public DocumentRevision getBaseRevision() {
        return baseRevision;
    }

    @Override
    public DocumentRevision getHeadRevision() {
        return headRevision;
    }

    @Override
    public RevisionMetadata getMetadataForRevision(DocumentRevision revision) {
        return logs.get(revision);
    }

    @Override
    public List<OWLOntologyChange> getChangesForRevision(DocumentRevision revision) {
        return revisions.get(revision);
    }

    @Override
    public SortedMap<DocumentRevision, List<OWLOntologyChange>> getRevisions() {
        return revisions;
    }

    @Override
    public SortedMap<DocumentRevision, RevisionMetadata> getMetadata() {
        return logs;
    }

    @Override
    public boolean isEmpty() {
        return revisions.isEmpty();
    }

    @Override
    public int hashCode() {
        return getBaseRevision().hashCode() + getHeadRevision().hashCode() + 42 * getRevisions().hashCode()
                + getMetadata().hashCode() / 42;
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
        return other.getBaseRevision().equals(getBaseRevision()) && other.getHeadRevision().equals(getHeadRevision())
                && other.getRevisions().equals(getRevisions())
                && other.getMetadata().equals(getMetadata());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append(baseRevision);
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
