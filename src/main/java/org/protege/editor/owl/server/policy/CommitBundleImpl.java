package org.protege.editor.owl.server.policy;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.List;

public class CommitBundleImpl implements CommitBundle {

    private static final long serialVersionUID = -6145139680901780930L;

    private RevisionMetadata metadata;
    private List<OWLOntologyChange> changes;
    private DocumentRevision startRevision;

    public CommitBundleImpl(final RevisionMetadata metadata, final List<OWLOntologyChange> changes, DocumentRevision headRevision) {
        this.metadata = metadata;
        this.changes = changes;
        this.startRevision = headRevision;
    }

    @Override
    public RevisionMetadata getRevisionMetadata() {
        return metadata;
    }

    @Override
    public List<OWLOntologyChange> getChanges() {
        return changes;
    }

    @Override
    public DocumentRevision getHeadRevision() {
        return startRevision;
    }
}
