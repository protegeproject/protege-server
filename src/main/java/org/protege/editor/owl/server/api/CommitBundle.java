package org.protege.editor.owl.server.api;

import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.List;

/**
 * Represents the whole commit changes that users send to the server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface CommitBundle {

    RevisionMetadata getRevisionMetadata();

    List<OWLOntologyChange> getChanges();

    DocumentRevision getHeadRevision();
}
