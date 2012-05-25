package org.protege.owl.server.api;

import java.util.Collection;

import org.semanticweb.owlapi.model.IRI;

public interface Server {
	
	ServerPath getServerDocument(User u, IRI serverIRI);
	
	Collection<ServerPath> list(User u, ServerDirectory dir);
		
	ChangeDocument getChanges(User u, OntologyDocument doc, ServerRevision start, ServerRevision end);
	
	OntologyDocument create(User u, String commitComment, OntologyDocument doc);
	
	void applyChange(User u, String commitComment, OntologyDocument doc, ServerRevision revision, ChangeDocument changes);
}
