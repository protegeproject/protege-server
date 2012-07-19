package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.OWLOntology;

public class VersionedOWLOntology {
	private OWLOntology ontology;
	private RemoteOntologyDocument serverDocument;
	private OntologyDocumentRevision revision;
	
	
	public VersionedOWLOntology(OWLOntology ontology,
								 RemoteOntologyDocument serverDocument,
								 OntologyDocumentRevision revision) {
		this.ontology = ontology;
		this.serverDocument = serverDocument;
		this.revision = revision;
	}


	public OWLOntology getOntology() {
		return ontology;
	}


	public RemoteOntologyDocument getServerDocument() {
		return serverDocument;
	}
	
	public OntologyDocumentRevision getRevision() {
		return revision;
	}
	
	public void setRevision(OntologyDocumentRevision revision) {
		this.revision = revision;
	}
	
}
