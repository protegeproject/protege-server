package org.protege.owl.server.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

// TODO - perhaps these should not be IOExceptions.
public interface Server {
	
	ServerDocument getServerDocument(User u, IRI serverIRI) throws DocumentNotFoundException;
	
	Collection<ServerDocument> list(User u, ServerDirectory dir) throws IOException;
		
	RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws IOException;
	
	ServerDirectory createServerDirectory(User u, IRI serverIRI) throws IOException;
	
	ChangeDocument getChanges(User u, RemoteOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException;

	void applyChange(User u, RemoteOntologyDocument doc, String commitComment, OntologyDocumentRevision revision, ChangeDocument changes) throws IOException;
}
