package org.protege.owl.server.api;

import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.api.ChangeHistory;
import org.protege.owl.server.changes.api.ServerOntologyDocument;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.List;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface ChangeService {

    ChangeHistory getChanges(ServerOntologyDocument doc, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception;

    List<OWLOntologyChange> getConflicts(ServerOntologyDocument doc, CommitBundle changes) throws Exception;
}
