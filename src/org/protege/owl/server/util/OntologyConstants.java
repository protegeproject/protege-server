package org.protege.owl.server.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLStringLiteral;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class OntologyConstants {
	public static final OWLDataFactory factory = OWLDataFactoryImpl.getInstance();

	public static final String NS = "http://semanticweb.org/owlapi/internal/Server.owl";
	public static final OWLClass REMOTE_ONTOLOGY_CLASS = OntologyConstants.factory.getOWLClass(IRI.create(NS + "#RemoteOntology"));

	public static final OWLDataProperty ONTOLOGY_NAME_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasOntologyName"));
	public static final OWLDataProperty ONTOLOGY_SHORT_NAME_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#ontologyShortName"));
	public static final OWLDataProperty ONTOLOGY_MAX_REVISION_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasMaxOntologyRevision"));
	public static final OWLDataProperty ONTOLOGY_MARKED_REVISION_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasMarkedRevision"));
	public static final OWLDataProperty ONTOLOGY_IMPORTS = factory.getOWLDataProperty(IRI.create(NS + "#imports"));
	
	public static final OWLNamedIndividual REMOTE_ONTOLOGY_INSTANCE = factory.getOWLNamedIndividual(IRI.create(NS + "#RemoteOntologyInstance"));

	public static final OWLAnnotationProperty AXIOM_ACTION = factory.getOWLAnnotationProperty(IRI.create(NS + "#RemoteAxiomAction"));

	public static final OWLStringLiteral AXIOM_ADDED = factory.getOWLStringLiteral("Axiom Added");
	public static final OWLStringLiteral AXIOM_REMOVED = factory.getOWLStringLiteral("Axiom Removed");
	
	public static final OWLAnnotation ADD_AXIOM_ANNOTATION = factory.getOWLAnnotation(OntologyConstants.AXIOM_ACTION, OntologyConstants.AXIOM_ADDED);
	public static final OWLAnnotation REMOVE_AXIOM_ANNOTATION = factory.getOWLAnnotation(OntologyConstants.AXIOM_ACTION, OntologyConstants.AXIOM_REMOVED);
	
}
