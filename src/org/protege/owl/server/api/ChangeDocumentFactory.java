package org.protege.owl.server.api;

import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyChange;

public interface ChangeDocumentFactory {
	ChangeDocument createChangeDocument(OntologyDocument doc, List<OWLOntologyChange> changes, ServerRevision start, ServerRevision end);
}
