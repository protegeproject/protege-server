package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;


/**
 * From the OWL 2 Specification: "Each ontology is associated with an <em>ontology document</em>,
 * which physically contains the ontology stored in a particular way.  The name "ontology document"
 * reflects the expectation that a large number of ontologies will be stored in physical text 
 * documents written in one of the syntaxes of OWL 2."
 * 
 * This is a 
 * 
 * 
 * @author tredmond
 *
 */
public class RemoteOntologyDocument extends ServerDocument {
	private OntologyDocumentRevision revision;
	
	public RemoteOntologyDocument(IRI backingStore, OntologyDocumentRevision revision) {
		super(backingStore);
		this.revision = revision;
	}
	
	public OntologyDocumentRevision getRevision() {
		return revision;
	}
	
	public void setRevision(OntologyDocumentRevision revision) {
		this.revision = revision;
	}

}
