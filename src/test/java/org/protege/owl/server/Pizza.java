package org.protege.owl.server;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

public class Pizza {

	public static final String PIZZA_LOCATION = "src/test/resources/pizza.owl";
	public static final String PIZZA_NS = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
	
	public static final OWLClass CHEESEY_PIZZA;
	
	static {
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		CHEESEY_PIZZA = factory.getOWLClass(IRI.create(PIZZA_NS + "#CheeseyPizza"));
	}
	
	private Pizza() {
		
	}
	
	
}
