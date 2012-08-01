package org.protege.owl.server;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class PizzaVocabulary {

	public static final String PIZZA_LOCATION = "src/test/resources/pizza.owl";
	public static final String PIZZA_NS = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
	
	public static final OWLClass CHEESEY_PIZZA;
	
	static {
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		CHEESEY_PIZZA = factory.getOWLClass(IRI.create(PIZZA_NS + "#CheeseyPizza"));
	}
	
	
	public static OWLOntology loadPizza() throws OWLOntologyCreationException {
	    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    return manager.loadOntologyFromOntologyDocument(new File(PIZZA_LOCATION));
	}
	
	private PizzaVocabulary() {
		
	}
	
	
}
