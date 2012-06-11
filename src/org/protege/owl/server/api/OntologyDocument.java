package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.IRI;


/**
 * From the OWL 2 Specification: "Each ontology is associated with an <em>ontology document</em>,
 * which physically contains the ontology stored in a particular way.  The name "ontology document"
 * reflects the expectation that a large number of ontologies will be stored in physical text 
 * documents written in one of the syntaxes of OWL 2."
 * 
 * @author tredmond
 *
 */
public interface OntologyDocument {

	boolean hasDocumentLocation();
	
	IRI getDocumentLocation();
	
	
	boolean hasVersionControl();
	
	OntologyDocumentRevision getCurrentRevision();
	
	ChangeDocument getLocalHistory();
	
	void saveHistory(ChangeDocument newChanges);

		
	boolean hasBackingStore();
	
	IRI getBackingStore();

}
