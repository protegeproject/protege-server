package org.protege.owl.server.util;

import static org.protege.owl.server.util.OntologyConstants.ADD_AXIOM_ANNOTATION;
import static org.protege.owl.server.util.OntologyConstants.AXIOM_ABOUT;
import static org.protege.owl.server.util.OntologyConstants.NS;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_CURRENT_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_IMPORTS;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_CLASS;
import static org.protege.owl.server.util.OntologyConstants.REMOVE_AXIOM_ANNOTATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class ChangeToAxiomConverter implements OWLOntologyChangeVisitor {
    private int counter = 0;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private OWLOntology metaOntology;
	private AddRemoveAxiomVisitor visitor = new AddRemoveAxiomVisitor();
	private Map<IRI, OWLAnonymousIndividual> nameToRemoteOntologyInstanceMap = new HashMap<IRI, OWLAnonymousIndividual>();
	
	
	public ChangeToAxiomConverter() throws OWLOntologyCreationException {
		init();
	}
	
	public void init() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		metaOntology = manager.createOntology(IRI.create(NS));
		
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		OWLAxiom decl = factory.getOWLDeclarationAxiom(REMOTE_ONTOLOGY_CLASS);
		changes.add(new AddAxiom(metaOntology, decl));
		manager.applyChanges(changes);
	}
	
	public OWLAnonymousIndividual getOntologyDecl(OWLOntology realOntology) {
	    IRI ontologyName = realOntology.getOntologyID().getOntologyIRI();
	    OWLAnonymousIndividual o = nameToRemoteOntologyInstanceMap.get(ontologyName);
	    if (o == null) {
	        o = factory.getOWLAnonymousIndividual();
	        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
	        changes.add(new AddAxiom(metaOntology, factory.getOWLClassAssertionAxiom(REMOTE_ONTOLOGY_CLASS, o)));
	        changes.add(new AddAxiom(metaOntology, 
	                                 factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_NAME_PROPERTY, o, factory.getOWLLiteral(ontologyName.toString()))));
	        manager.applyChanges(changes);
	        nameToRemoteOntologyInstanceMap.put(ontologyName, o);
	    }
	    return o;
	}
	
	public void addRevisionInfo(OWLOntology realOntology, int revision) {
	    OWLAnonymousIndividual o = getOntologyDecl(realOntology);
        manager.addAxiom(metaOntology, factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_CURRENT_REVISION_PROPERTY, o, factory.getOWLLiteral(revision)));
	}
	
	public OWLOntology getMetaOntology() {
		return metaOntology;
	}

	
	public void visit(AddAxiom change) {
		visitor.setAddAxiom(true);
		visitor.setOntology(change.getOntology());
		change.getAxiom().accept(visitor);
		manager.addAxiom(metaOntology, visitor.getOWLAxiom());
	}

	
	public void visit(RemoveAxiom change) {
		visitor.setAddAxiom(false);
		visitor.setOntology(change.getOntology());
		change.getAxiom().accept(visitor);
		manager.addAxiom(metaOntology, visitor.getOWLAxiom());
	}

	
	public void visit(SetOntologyID change) {
		// SetOntologyId calls are ignored.
	}

	
	public void visit(AddImport change) {
	    OWLLiteral importAsLiteral = factory.getOWLLiteral(change.getImportDeclaration().getIRI().toString());
	    Set<OWLAnnotation> importAnnotations = new HashSet<OWLAnnotation>();
	    importAnnotations.add(REMOVE_AXIOM_ANNOTATION);
	    importAnnotations.add(getAboutAnnotation(change.getOntology()));
		OWLAxiom doImport = factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_IMPORTS, 
																	 getOntologyDecl(change.getOntology()), 
																	 importAsLiteral,
																	 importAnnotations);
		manager.addAxiom(metaOntology, doImport);
	}

	
	public void visit(RemoveImport change) {
		OWLLiteral importAsLiteral = factory.getOWLLiteral(change.getImportDeclaration().getIRI().toString());
		Set<OWLAnnotation> importAnnotations = new HashSet<OWLAnnotation>();
		importAnnotations.add(REMOVE_AXIOM_ANNOTATION);
		importAnnotations.add(getAboutAnnotation(change.getOntology()));
		OWLAxiom removeImport = factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_IMPORTS, 
		                                                                 getOntologyDecl(change.getOntology()), 
																		 importAsLiteral,
																		 importAnnotations);
		manager.addAxiom(metaOntology, removeImport);	
	}

	
	public void visit(AddOntologyAnnotation change) {
		OWLAnnotation annotation = change.getAnnotation();
		Set<OWLAnnotation> annotationAnnotations = new HashSet<OWLAnnotation>(annotation.getAnnotations());
		annotationAnnotations.add(ADD_AXIOM_ANNOTATION);
		annotationAnnotations.add(getAboutAnnotation(change.getOntology()));
		OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(getOntologyDecl(change.getOntology()),
																annotation,
																annotationAnnotations);
		manager.addAxiom(metaOntology, axiom);
	}

	
	public void visit(RemoveOntologyAnnotation change) {
		OWLAnnotation annotation = change.getAnnotation();
		Set<OWLAnnotation> annotationAnnotations = new HashSet<OWLAnnotation>(annotation.getAnnotations());
		annotationAnnotations.add(REMOVE_AXIOM_ANNOTATION);
		annotationAnnotations.add(getAboutAnnotation(change.getOntology()));
		OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(getOntologyDecl(change.getOntology()),
																annotation,
																annotationAnnotations);
		manager.addAxiom(metaOntology, axiom);
	}
	
	private OWLAnnotation getAboutAnnotation(OWLOntology ontology) {
	    return factory.getOWLAnnotation(AXIOM_ABOUT, getOntologyDecl(ontology));
	}
	
	
	private class AddRemoveAxiomVisitor extends AxiomAnnotationsVisitor {
		private boolean addAxiom;
		private OWLOntology ontology;

		public void setAddAxiom(boolean addAxiom) {
			this.addAxiom = addAxiom;
		}
		
		public void setOntology(OWLOntology ontology) {
		    this.ontology = ontology;
		}
		
		protected Set<OWLAnnotation> processAnnotations(Set<OWLAnnotation> axiomAnnotations) {
			Set<OWLAnnotation> newAxiomAnnotations = new HashSet<OWLAnnotation>(axiomAnnotations);
			newAxiomAnnotations.add(addAxiom ? ADD_AXIOM_ANNOTATION : REMOVE_AXIOM_ANNOTATION);
			newAxiomAnnotations.add(getAboutAnnotation(ontology));
			return newAxiomAnnotations;
		}
	}
}
