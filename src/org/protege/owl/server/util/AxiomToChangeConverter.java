package org.protege.owl.server.util;

import static org.protege.owl.server.util.OntologyConstants.ADD_AXIOM_ANNOTATION;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_IMPORTS;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_INSTANCE;
import static org.protege.owl.server.util.OntologyConstants.REMOVE_AXIOM_ANNOTATION;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;

public class AxiomToChangeConverter extends AxiomAnnotationsVisitor {
	private OWLOntology ontology;
	private OWLOntologyChange change;
	private boolean isAdd    = false;
	private boolean isRemove = false;
	
	public AxiomToChangeConverter(OWLOntology ontology) {
		this.ontology = ontology;
	}
	
	public void clear() {
		change = null;
		isAdd = false;
		isRemove = false;
	}
	
	public OWLOntologyChange getChange() {
		try {
			if (change != null) {
				return change;
			}
			if (!isAdd && !isRemove) {
				return null;
			}
			else if (isAdd) {
				return new AddAxiom(ontology, getOWLAxiom());
			}
			else {
				return new RemoveAxiom(ontology, getOWLAxiom());
			}
		}
		finally {
			clear();
		}
	}

	@Override
	protected Set<OWLAnnotation> processAnnotations(Set<OWLAnnotation> axiomAnnotations) {
		axiomAnnotations = new HashSet<OWLAnnotation>(axiomAnnotations);
		if (!(isAdd = axiomAnnotations.remove(ADD_AXIOM_ANNOTATION))) {
			isRemove = axiomAnnotations.remove(REMOVE_AXIOM_ANNOTATION);
		}
		return axiomAnnotations;
	}
	
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		super.visit(axiom);
		if (!isAdd && !isRemove) {
			return;
		}
		if (axiom.getSubject().equals(REMOTE_ONTOLOGY_INSTANCE) && axiom.getProperty().equals(ONTOLOGY_IMPORTS)) {
			OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
			OWLImportsDeclaration declaration = factory.getOWLImportsDeclaration(IRI.create(axiom.getObject().getLiteral()));
			if (isAdd) {
				change = new AddImport(ontology, declaration);
			}
			else {
				change = new RemoveImport(ontology, declaration);
			}
		}
	}
	
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		super.visit(axiom);
		if (!isAdd && !isRemove) {
			return;
		}
		axiom = (OWLAnnotationAssertionAxiom) getOWLAxiom();
		if (axiom.getSubject().equals(REMOTE_ONTOLOGY_INSTANCE.getIRI())) {
			for (OWLAnnotation annotation :axiom.getAnnotations()) {
				if (isAdd) {
					change = new AddOntologyAnnotation(ontology, annotation);
				}
				else {
					change = new RemoveOntologyAnnotation(ontology, annotation);
				}
			}
		}
	}

}
