package org.protege.owl.server.api;

import java.util.Collection;

import org.semanticweb.owlapi.model.IRI;

public interface Server {
	
	ServerDocument getServerDocument(User u, IRI serverIRI);
	
	Collection<ServerDocument> list(User u, ServerDirectory dir);
		
	OntologyDocument createOntologyDocument(User u, String commitComment, IRI serverIRI);
	
	ServerDirectory createServerDirectory(User u, IRI serverIRI);
	
	ChangeDocument getChanges(User u, OntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end);

	void applyChange(User u, OntologyDocument doc, String commitComment, OntologyDocumentRevision revision, ChangeDocument changes);
}
