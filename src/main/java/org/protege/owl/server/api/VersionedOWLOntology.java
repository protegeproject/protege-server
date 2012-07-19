package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This is an OWL ontology held by the client with an association to a corresponding server
 * document at a particular revision.  
 * <p/>
 * There is enough information here that several operations are possible:
 * <ul>
 * <li>the changes made to the local in-memory ontology can be committed to the remote server.  
 *     The collection of changes can be calculated by getting the changes up to the revision of the in-memory
 *     ontology and calculating the changes made since the changes described on the server.  ClientUtilities uses 
 *     just such a method internally.</li>
 * <li>the changes made on the server since the revision can be merged into the clients copy of the ontology</li>
 * </ul>
 * 
 * 
 * @author redmond
 *
 */
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
