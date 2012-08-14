package org.protege.owl.server.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public interface DocumentFactory {
    
    ChangeDocument createEmptyChangeDocument(OntologyDocumentRevision revision);
	
	/**
	 * Create a change document for the list of changes and the commit comments.
	 * 
	 * @param doc
	 * @param changes
	 * @param start
	 * @return
	 */
	ChangeDocument createChangeDocument(List<OWLOntologyChange> changes, ChangeMetaData metaData, OntologyDocumentRevision start);
	
	ChangeDocument readChangeDocument(InputStream in, OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException;
	
	boolean hasServerMetadata(OWLOntology ontology);

	VersionedOWLOntology createVersionedOntology(OWLOntology ontology) throws IOException;
	
	VersionedOWLOntology createVersionedOntology(OWLOntology ontology, RemoteOntologyDocument serverDocument, OntologyDocumentRevision revision);
	

}
