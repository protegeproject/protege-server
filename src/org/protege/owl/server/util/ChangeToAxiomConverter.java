package org.protege.owl.server.util;


import static org.protege.owl.server.util.OntologyConstants.ADD_AXIOM_ANNOTATION;
import static org.protege.owl.server.util.OntologyConstants.NS;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_IMPORTS;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_CLASS;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_INSTANCE;
import static org.protege.owl.server.util.OntologyConstants.REMOVE_AXIOM_ANNOTATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLStringLiteral;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class ChangeToAxiomConverter implements OWLOntologyChangeVisitor {
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private OWLOntology ontology;
	private AddRemoveAxiomVisitor visitor = new AddRemoveAxiomVisitor();
	
	public ChangeToAxiomConverter() throws OWLOntologyCreationException {
		init();
	}
	
	public void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontology = manager.createOntology(IRI.create(NS));
		
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		OWLAxiom decl = factory.getOWLDeclarationAxiom(REMOTE_ONTOLOGY_CLASS);
		changes.add(new AddAxiom(ontology, decl));
		decl = factory.getOWLClassAssertionAxiom(REMOTE_ONTOLOGY_CLASS, REMOTE_ONTOLOGY_INSTANCE);
		changes.add(new AddAxiom(ontology, decl));
		manager.applyChanges(changes);
	}
	
	public OWLOntology getOntology() {
		return ontology;
	}

	@Override
	public void visit(AddAxiom change) {
		visitor.setAddAxiom(true);
		change.getAxiom().accept(visitor);
		manager.addAxiom(ontology, visitor.getOWLAxiom());
	}

	@Override
	public void visit(RemoveAxiom change) {
		visitor.setAddAxiom(false);
		change.getAxiom().accept(visitor);
		manager.addAxiom(ontology, visitor.getOWLAxiom());
	}

	@Override
	public void visit(SetOntologyID change) {
		// SetOntologyId calls are ignored.
	}

	@Override
	public void visit(AddImport change) {
		OWLStringLiteral importAsLiteral = factory.getOWLStringLiteral(change.getImportDeclaration().getIRI().toString());
		OWLAxiom doImport = factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_IMPORTS, 
																	 REMOTE_ONTOLOGY_INSTANCE, 
																	 importAsLiteral,
																	 Collections.singleton(ADD_AXIOM_ANNOTATION));
		manager.addAxiom(ontology, doImport);
	}

	@Override
	public void visit(RemoveImport change) {
		OWLStringLiteral importAsLiteral = factory.getOWLStringLiteral(change.getImportDeclaration().getIRI().toString());
		OWLAxiom removeImport = factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_IMPORTS, 
																		 REMOTE_ONTOLOGY_INSTANCE, 
																		 importAsLiteral,
																		 Collections.singleton(REMOVE_AXIOM_ANNOTATION));
		manager.addAxiom(ontology, removeImport);	
	}

	@Override
	public void visit(AddOntologyAnnotation change) {
		OWLAnnotation annotation = change.getAnnotation();
		Set<OWLAnnotation> annotationAnnotations = new HashSet<OWLAnnotation>(annotation.getAnnotations());
		annotationAnnotations.add(ADD_AXIOM_ANNOTATION);
		OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(REMOTE_ONTOLOGY_INSTANCE.getIRI(),
																annotation,
																annotationAnnotations);
		manager.addAxiom(ontology, axiom);
	}

	@Override
	public void visit(RemoveOntologyAnnotation change) {
		OWLAnnotation annotation = change.getAnnotation();
		Set<OWLAnnotation> annotationAnnotations = new HashSet<OWLAnnotation>(annotation.getAnnotations());
		annotationAnnotations.add(REMOVE_AXIOM_ANNOTATION);
		OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(REMOTE_ONTOLOGY_INSTANCE.getIRI(),
																annotation,
																annotationAnnotations);
		manager.addAxiom(ontology, axiom);
	}
	
	
	private class AddRemoveAxiomVisitor extends AxiomAnnotationsVisitor {
		private boolean addAxiom;

		public void setAddAxiom(boolean addAxiom) {
			this.addAxiom = addAxiom;
		}
		
		public boolean isAddAxiom() {
			return addAxiom;
		}
		
		@Override
		protected Set<OWLAnnotation> processAnnotations(Set<OWLAnnotation> axiomAnnotations) {
			Set<OWLAnnotation> newAxiomAnnotations = new HashSet<OWLAnnotation>(axiomAnnotations);
			newAxiomAnnotations.add(addAxiom ? ADD_AXIOM_ANNOTATION : REMOVE_AXIOM_ANNOTATION);
			return newAxiomAnnotations;
		}
	}
}
