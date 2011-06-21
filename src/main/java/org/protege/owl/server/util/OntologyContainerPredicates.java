package org.protege.owl.server.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;

public interface OntologyContainerPredicates {
    boolean contains(OWLOntology ontology, OWLAxiom axiom);
    boolean contains(OWLOntology ontology, OWLImportsDeclaration owlImport);
    boolean contains(OWLOntology ontology, OWLAnnotation annotation);
}
