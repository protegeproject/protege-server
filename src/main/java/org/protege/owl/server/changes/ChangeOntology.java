package org.protege.owl.server.changes;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataFactory;

/**
 * 
 * @author tredmond
 * @deprecated replace with Matthew's binary serialization format.
 */
@Deprecated
public class ChangeOntology {
	public static final String NS = "http://protege.org/ontologies/ChangeSerializationOntology.owl";
	
	public static final OWLAnnotationProperty IS_AXIOM_ADDED;
	public static final OWLAnnotationProperty REVISION;
	public static final OWLAnnotationProperty SET_ONTOLOGY_ID;
	public static final OWLAnnotationProperty SET_ONTOLOGY_VERSION;
	public static final OWLAnnotationProperty IMPORTS;
	public static final OWLAnnotationProperty COMMIT_COMMENT;
	
	static {
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		IS_AXIOM_ADDED       = factory.getOWLAnnotationProperty(IRI.create(NS + "#isAxiomAdded"));
		REVISION             = factory.getOWLAnnotationProperty(IRI.create(NS + "#hasRevision"));
		SET_ONTOLOGY_ID      = factory.getOWLAnnotationProperty(IRI.create(NS + "#setOntlogyId"));
		SET_ONTOLOGY_VERSION = factory.getOWLAnnotationProperty(IRI.create(NS + "#setOntologyVersion"));
		IMPORTS              = factory.getOWLAnnotationProperty(IRI.create(NS + "#imports"));
		COMMIT_COMMENT       = factory.getOWLAnnotationProperty(IRI.create(NS + "#commitComment"));
	}
	
	private ChangeOntology() {
		
	}
}
