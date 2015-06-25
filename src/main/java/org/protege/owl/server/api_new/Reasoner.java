package org.protege.owl.server.api_new;


import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

/**
 * A representation of a reasoner
 *
 * @author Rafael Gonçalves
 * Stanford Center for Biomedical Informatics Research
 */
public interface Reasoner {

    /**
     * Classify the given ontology and return the set of inferred axioms
     *
     * @param ontology  OWL ontology
     * @return Set of inferred OWL axioms
     */
    Set<OWLAxiom> classify(OWLOntology ontology);

    /**
     * Check if given ontology is consistent
     *
     * @param ontology  OWL ontology
     * @return true if ontology is consistent, false otherwise
     */
    boolean isConsistent(OWLOntology ontology);

    /**
     * Check if given axiom is entailed by the specified ontology
     *
     * @param axiom OWL axiom
     * @param ontology  OWL ontology
     * @return true if axiom is entailed by the ontology, false otherwise
     */
    boolean isEntailed(OWLAxiom axiom, OWLOntology ontology);

    /**
     * Check if the given class expression is satisfiable with respect to the specified ontology
     *
     * @param classExpression   OWL class expression
     * @param ontology  OWL ontology
     * @return true if class expression is satisfiable, false otherwise
     */
    boolean isSatisfiable(OWLClassExpression classExpression, OWLOntology ontology);

}
