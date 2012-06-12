package org.protege.owl.server;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

public class TestVocabulary {
	public static final String NS = "http://protege.org/ontologies/TestServer.owl";
	public static final String NS2 = "http://protege.org/ontologies/TestServer2.owl";

	public static final OWLClass A;
	public static final OWLClass B;
	public static final OWLClass C;
	
	public static final OWLAnnotationProperty AP;
	
	public static final OWLAxiom AXIOM1;
	public static final OWLAxiom AXIOM2;
	
	static {
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		A = factory.getOWLClass(IRI.create(NS + "#A"));
		B = factory.getOWLClass(IRI.create(NS + "#B"));
		C = factory.getOWLClass(IRI.create(NS + "#C"));
		
		AP = factory.getOWLAnnotationProperty(IRI.create(NS + "#annotationProperty"));
		
		AXIOM1 = factory.getOWLEquivalentClassesAxiom(A, B, C);
		AXIOM2 = factory.getOWLDisjointClassesAxiom(A, B, C);
	}
	
	private TestVocabulary() {
		
	}
}
