package org.protege.editor.owl.server.policy;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.versioning.DocumentRevision;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.List;

public class CommitBundleImpl implements CommitBundle {

    private List<OWLOntologyChange> changes;
    private DocumentRevision startRevision;

    public CommitBundleImpl(final List<OWLOntologyChange> changes, DocumentRevision headRevision) {
        this.changes = changes;
        this.startRevision = headRevision;
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
