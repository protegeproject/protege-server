package org.protege.owl.server.api;

import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public interface Client {
	
	Collection<ServerPath> list(ServerPath path);
	
	void create(String commitComment, OntologyDocument doc);
	
	ChangeDocument getChanges(OntologyDocument document, ServerRevision start, ServerRevision end);
	
	void commit(OntologyDocument document, ChangeDocument changes, ServerRevision start, ServerRevision end);
	
	
	/*
	 * Calls involving OWLOntology...
	 */
	
	OntologyDocument getOntologyDocument(OWLOntology ontology);
	
	OWLOntology get(OWLOntologyManager manager, OntologyDocument doc);
	
	void commit(String commitComment, OWLOntology ontology);
	
	void update(ServerRevision revision, OWLOntology ontology);
	
	List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology);
}
