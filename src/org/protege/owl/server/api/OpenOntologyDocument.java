package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.OWLOntology;

public class OpenOntologyDocument {
	private OWLOntology ontology;
	private RemoteOntologyDocument serverDocument;
	
	
	public OpenOntologyDocument(OWLOntology ontology,
								RemoteOntologyDocument serverDocument) {
		this.ontology = ontology;
		this.serverDocument = serverDocument;
	}


	public OWLOntology getOntology() {
		return ontology;
	}


	public RemoteOntologyDocument getServerDocument() {
		return serverDocument;
	}
	
}
