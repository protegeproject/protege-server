package org.protege.owl.server.api;

import java.util.Collection;

import org.semanticweb.owlapi.model.IRI;

public interface Client {
	
	DocumentFactory getDocumentFactory();
	
	ServerDocument getServerDocument(IRI serverIRI);
	
	Collection<ServerDocument> list(ServerDocument path);

	OntologyDocumentRevision getHeadRevision(OntologyDocument doc);
	
	void create(String commitComment, OntologyDocument doc);
		
	/**
	 * If the end revision is null this means get all changes up to the latest.
	 * 
	 * @param document
	 * @param start
	 * @param end
	 * @return
	 */
	ChangeDocument getChanges(OntologyDocument document, OntologyDocumentRevision start, OntologyDocumentRevision end);
	
	void commit(OntologyDocument document, OntologyDocumentRevision revision, ChangeDocument changes);

}
