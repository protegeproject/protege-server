package org.protege.owl.server.api;

import java.util.Collection;

import org.semanticweb.owlapi.model.IRI;

public interface Client {
	
	ChangeDocumentFactory getChangeDocumentFactory();
	
	OntologyDocument getOntologyDocument(IRI ontologyIRI);
	
	ServerRevision getServerRevision(IRI ontologyIRI);
	
	Collection<ServerPath> list(ServerPath path);
	
	void create(String commitComment, OntologyDocument doc);
		
	ChangeDocument getChanges(OntologyDocument document, ServerRevision start, ServerRevision end);
	
	void commit(OntologyDocument document, ServerRevision revision, ChangeDocument changes);

}
