package org.protege.owl.server.api;

import org.protege.owl.server.versioning.OntologyDocumentRevision;

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

    OntologyDocumentRevision getStartRevision();

    OntologyDocumentRevision getEndRevision();
}
