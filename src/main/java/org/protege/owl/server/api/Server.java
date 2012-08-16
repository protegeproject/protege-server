package org.protege.owl.server.api;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;


public interface Server {
		
	ServerDocument getServerDocument(User u, IRI serverIRI) throws OWLServerException;
	
	Collection<ServerDocument> list(User u, ServerDirectory dir) throws OWLServerException;
		
	ServerDirectory createDirectory(User u, IRI serverIRI) throws OWLServerException;

	RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws OWLServerException;
	
	ChangeDocument getChanges(User u, RemoteOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException;

	ChangeDocument commit(User u, RemoteOntologyDocument doc, ChangeMetaData commitComment, ChangeDocument changes, SortedSet<OntologyDocumentRevision> myCommits) throws OWLServerException;
	
	void shutdown();
	
	/* Interfaces that are not visible to the client. */
	
   File getConfiguration(String fileName) throws OWLServerException;
	    
   File getConfiguration(ServerDocument doc, String extension) throws OWLServerException;
   
   void setTransports(Collection<ServerTransport> transports);
   
   Collection<ServerTransport> getTransports();
	
}
