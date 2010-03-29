package org.protege.owl.server.util;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;

public class AxiomToChangeConverter extends AxiomAnnotationsVisitor {
	private OWLOntology ontology;
	
	public AxiomToChangeConverter(OWLOntology ontology) {
		this.ontology = ontology;
	}

	@Override
	protected Set<OWLAnnotation> processAnnotations(
			Set<OWLAnnotation> axiomAnnotations) {
		// TODO Auto-generated method stub
		return null;
	}

}
