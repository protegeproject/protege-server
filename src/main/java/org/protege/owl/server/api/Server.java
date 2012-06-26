package org.protege.owl.server.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.protege.owl.server.api.exception.DocumentNotFoundException;
import org.semanticweb.owlapi.model.IRI;

// TODO - perhaps these should not be IOExceptions.
public interface Server {
		
	ServerDocument getServerDocument(User u, IRI serverIRI) throws DocumentNotFoundException;
	
	Collection<ServerDocument> list(User u, ServerDirectory dir) throws IOException;
		
	ServerDirectory createDirectory(User u, IRI serverIRI) throws IOException;

	RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws IOException;
	
	ChangeDocument getChanges(User u, RemoteOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException;

	void commit(User u, RemoteOntologyDocument doc, String commitComment, ChangeDocument changes) throws IOException;
	
	CommitWhiteBoard getCommitWhiteBoard();
	
	void shutdown();
}
