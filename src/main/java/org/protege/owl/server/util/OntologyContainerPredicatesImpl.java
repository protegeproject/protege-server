package org.protege.owl.server.util;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;

public class OntologyContainerPredicatesImpl implements
		OntologyContainerPredicates {

	@Override
	public boolean contains(OWLOntology ontology, OWLAnnotation annotation) {
		return ontology.getAnnotations().contains(annotation);
	}
	
	@Override
	public boolean contains(OWLOntology ontology, OWLImportsDeclaration owlImport) {
		return ontology.getImportsDeclarations().contains(owlImport);
	}
	
	@Override
	public boolean contains(OWLOntology ontology, OWLAxiom axiom) {
		return ontology.containsAxiom(axiom);
	}

}
