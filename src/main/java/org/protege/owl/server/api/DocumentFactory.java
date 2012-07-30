package org.protege.owl.server.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;
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
	ChangeDocument createChangeDocument(List<OWLOntologyChange> changes, Map<OntologyDocumentRevision, ChangeMetaData> metaData, OntologyDocumentRevision start);
	
	boolean hasServerMetadata(OWLOntology ontology);

	VersionedOWLOntology createVersionedOntology(OWLOntology ontology) throws IOException;
	
	VersionedOWLOntology createVersionedOntology(OWLOntology ontology, RemoteOntologyDocument serverDocument, OntologyDocumentRevision revision);
	

}
