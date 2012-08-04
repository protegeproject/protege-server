package org.protege.owl.server;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class PizzaVocabulary {

	public static final String PIZZA_LOCATION = "src/test/resources/pizza.owl";
	public static final String PIZZA_NS = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
	
	public static final OWLClass CHEESE_TOPPING;
	public static final OWLClass CHEESEY_PIZZA;
	public static final OWLClass PIZZA;
    public static final OWLClass TOMATO_TOPPING;
	
	public static final OWLObjectProperty HAS_TOPPING;
	
	public static final OWLAxiom CHEESEY_PIZZA_DEFINITION;
	public static final OWLAxiom NOT_CHEESEY_PIZZA_DEFINITION;
	
	static {
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		CHEESE_TOPPING = factory.getOWLClass(IRI.create(PIZZA_NS + "#CheeseTopping"));
		CHEESEY_PIZZA = factory.getOWLClass(IRI.create(PIZZA_NS + "#CheeseyPizza"));
		PIZZA = factory.getOWLClass(IRI.create(PIZZA_NS + "#Pizza"));
		TOMATO_TOPPING = factory.getOWLClass(IRI.create(PIZZA_NS + "TomatoTopping"));
		
		HAS_TOPPING = factory.getOWLObjectProperty(IRI.create(PIZZA_NS + "#hasTopping"));
		
        CHEESEY_PIZZA_DEFINITION = factory.getOWLEquivalentClassesAxiom(CHEESEY_PIZZA, factory.getOWLObjectIntersectionOf(PIZZA, factory.getOWLObjectSomeValuesFrom(HAS_TOPPING, CHEESE_TOPPING)));
        NOT_CHEESEY_PIZZA_DEFINITION = factory.getOWLEquivalentClassesAxiom(CHEESEY_PIZZA, factory.getOWLObjectIntersectionOf(PIZZA, factory.getOWLObjectSomeValuesFrom(HAS_TOPPING, TOMATO_TOPPING)));
		
	}
	
	
	public static OWLOntology loadPizza() throws OWLOntologyCreationException {
	    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    return manager.loadOntologyFromOntologyDocument(new File(PIZZA_LOCATION));
	}
	
	private PizzaVocabulary() {
		
	}
	
	
}
