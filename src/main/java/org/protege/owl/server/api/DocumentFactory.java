package org.protege.owl.server.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.impl.RemoteOntologyDocumentImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public interface DocumentFactory {
	
	/**
	 * Create a change document for the list of changes and the commit comments.
	 * 
	 * @param doc
	 * @param changes
	 * @param start
	 * @return
	 */
	ChangeDocument createChangeDocument(List<OWLOntologyChange> changes, Map<OntologyDocumentRevision, String> commitComments, OntologyDocumentRevision start);
	
	/**
	 * Create and write the server metadata associated with a local iri (localIRI).
	 * <p/>
	 * This class is responsible for maintaining 
	 * 
	 * @param localIRI
	 * @param serverDocument
	 * @return
	 */
	VersionedOntologyDocument createSavedOntologyDocument(IRI localIRI, RemoteOntologyDocument serverDocument) throws IOException;
	
	VersionedOntologyDocument getSavedOntologyDocument(IRI localIRI) throws IOException;
}
