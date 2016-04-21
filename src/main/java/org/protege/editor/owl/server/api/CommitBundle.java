package org.protege.editor.owl.server.api;

import org.protege.editor.owl.server.versioning.DocumentRevision;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.List;

/**
 * Represents the whole commit changes that users send to the server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface CommitBundle {

    List<OWLOntologyChange> getChanges();

    DocumentRevision getStartRevision();
}
