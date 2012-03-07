package org.protege.owl.server.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class OntologyConstants {
	public static final OWLDataFactory factory = OWLManager.getOWLDataFactory();

	/**
	 * The name of the fake meta-ontologies created by a server and also the name space or terms used by that ontology.
	 */
	public static final String NS = "http://semanticweb.org/owlapi/internal/Server.owl";
	/**
	 * A class whose individuals represent ontologies held by the server.
	 */
	public static final OWLClass REMOTE_ONTOLOGY_CLASS = OntologyConstants.factory.getOWLClass(IRI.create(NS + "#RemoteOntology"));

	/**
	 * A data property that will retrieve the name of the ontology from a member of REMOTE_ONTOLOGY_CLASS.
	 */
	public static final OWLDataProperty ONTOLOGY_NAME_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasOntologyName"));
	/**
     * A data property that will retrieve the short name of the ontology from a member of REMOTE_ONTOLOGY_CLASS.
     */
	public static final OWLDataProperty ONTOLOGY_SHORT_NAME_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#ontologyShortName"));
	/**
	 * A data property that describes the current ontology revision of an ontology on the client.
	 */
	public static final OWLDataProperty ONTOLOGY_CURRENT_REVISION_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasCurrentOntologyRevision"));

	/**
     * A data property that will retrieve the max revision from a member of REMOTE_ONTOLOGY_CLASS.
     */
	public static final OWLDataProperty ONTOLOGY_MAX_REVISION_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasMaxOntologyRevision"));
	/**
	 * A data property that will retrieve the marked revisions from a member of REMOTE_ONTOLOGY_CLASS.
	 */
	public static final OWLDataProperty ONTOLOGY_MARKED_REVISION_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasMarkedRevision"));
	
	
	/**
	 * A property that is used in the representation of add/remove Import actions on an ontology.  These changes are represented
	 * by an ONTOLOGY_IMPORTS data property assertion axiom on an individual in REMOTE_ONTOLOGY_CLASS.  The add/remove attribute
	 * is represented by the AXIOM_ADDED/AXIOM_REMOVED AXIOM_ACTION annotation.
	 */
	public static final OWLDataProperty ONTOLOGY_IMPORTS = factory.getOWLDataProperty(IRI.create(NS + "#imports"));

	/**
	 * An annotation property that is added to an axiom to indicate whether this axiom should be added or removed from the 
	 * remote ontology.
	 */
	public static final OWLAnnotationProperty AXIOM_ACTION = factory.getOWLAnnotationProperty(IRI.create(NS + "#RemoteAxiomAction"));

	/**
	 * An annotation property that recovers the ontology to which a change should be applied.
	 */
	public static final OWLAnnotationProperty AXIOM_ABOUT = factory.getOWLAnnotationProperty(IRI.create(NS + "RemoteAxiomForOntology"));
	/**
	 * An AXIOM_ACTION, AXIOM_ADDED annotation on an axiom means that this axiom should be added to the remote ontology.
	 */
	public static final OWLLiteral AXIOM_ADDED = factory.getOWLLiteral("Axiom Added");
	/**
     * An AXIOM_ACTION, AXIOM_ADDED annotation on an axiom means that this axiom should be added to the remote ontology.
     */
	public static final OWLLiteral AXIOM_REMOVED = factory.getOWLLiteral("Axiom Removed");
	
	/**
	 * The add axiom annotation.
	 */
	public static final OWLAnnotation ADD_AXIOM_ANNOTATION = factory.getOWLAnnotation(OntologyConstants.AXIOM_ACTION, OntologyConstants.AXIOM_ADDED);
	/**
     * The remove axiom annotation.
     */
	public static final OWLAnnotation REMOVE_AXIOM_ANNOTATION = factory.getOWLAnnotation(OntologyConstants.AXIOM_ACTION, OntologyConstants.AXIOM_REMOVED);
	
}
