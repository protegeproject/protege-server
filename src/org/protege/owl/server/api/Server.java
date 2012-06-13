package org.protege.owl.server.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.semanticweb.owlapi.model.IRI;

public interface Server {
	
	ServerDocument getServerDocument(User u, IRI serverIRI) throws DocumentNotFoundException;
	
	Collection<ServerDocument> list(User u, ServerDirectory dir) throws IOException;
		
	RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings);
	
	ServerDirectory createServerDirectory(User u, IRI serverIRI);
	
	ChangeDocument getChanges(User u, RemoteOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end);

	void applyChange(User u, RemoteOntologyDocument doc, String commitComment, OntologyDocumentRevision revision, ChangeDocument changes);
}
