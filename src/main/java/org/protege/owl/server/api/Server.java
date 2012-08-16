package org.protege.owl.server.api;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.protege.owl.server.api.exception.ServerException;
import org.semanticweb.owlapi.model.IRI;


public interface Server {
		
	ServerDocument getServerDocument(User u, IRI serverIRI) throws ServerException;
	
	Collection<ServerDocument> list(User u, ServerDirectory dir) throws ServerException;
		
	ServerDirectory createDirectory(User u, IRI serverIRI) throws ServerException;

	RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws ServerException;
	
	ChangeDocument getChanges(User u, RemoteOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws ServerException;

	ChangeDocument commit(User u, RemoteOntologyDocument doc, ChangeMetaData commitComment, ChangeDocument changes, SortedSet<OntologyDocumentRevision> myCommits) throws ServerException;
	
	void shutdown();
	
	/* Interfaces that are not visible to the client. */
	
   File getConfiguration(String fileName) throws ServerException;
	    
   File getConfiguration(ServerDocument doc, String extension) throws ServerException;
   
   void setTransports(Collection<ServerTransport> transports);
   
   Collection<ServerTransport> getTransports();
	
}
