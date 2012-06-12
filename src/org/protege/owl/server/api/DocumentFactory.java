package org.protege.owl.server.api;

import java.util.List;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public interface DocumentFactory {
	
	/**
	 * 
	 * @param doc
	 * @param changes
	 * @param start
	 * @return
	 */
	ChangeDocument createChangeDocument(List<OWLOntologyChange> changes, OntologyDocumentRevision start);
	
	OntologyDocument createOntologyDocument(IRI localDoc, IRI serverAddress);
	
	OntologyDocument loadOntologyDocument(IRI localDoc);
	
	OntologyDocument saveOntologyDocument(OntologyDocument doc);
}
