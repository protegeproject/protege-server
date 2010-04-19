package org.protege.owl.server.util;

import static org.protege.owl.server.util.OntologyConstants.ADD_AXIOM_ANNOTATION;
import static org.protege.owl.server.util.OntologyConstants.AXIOM_ABOUT;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_CURRENT_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_IMPORTS;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_CLASS;
import static org.protege.owl.server.util.OntologyConstants.REMOVE_AXIOM_ANNOTATION;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;

public class AxiomToChangeConverter extends AxiomAnnotationsVisitor {
	private Map<OWLAnonymousIndividual, OWLOntology> ontologyMap;
	private Map<IRI, Integer> currentRevisionMap;
	private OWLAnonymousIndividual ontologyRepresentative;
	private OWLOntologyChange change;
	private boolean isAdd    = false;
	private boolean isRemove = false;

	
	public AxiomToChangeConverter(OWLOntology metaOntology, Set<OWLOntology> ontologies) {
	    retrieveOntologies(metaOntology, ontologies);
	}
	
	private Map<OWLAnonymousIndividual, OWLOntology> retrieveOntologies(final OWLOntology metaOntology, Set<OWLOntology> ontologies) {
	    ontologyMap = new HashMap<OWLAnonymousIndividual, OWLOntology>();
	    currentRevisionMap = new HashMap<IRI, Integer>();
	    for (OWLIndividual o : REMOTE_ONTOLOGY_CLASS.getIndividuals(metaOntology)) {
	        if (o instanceof OWLNamedIndividual) {
	            continue;
	        }
	        for (OWLDataPropertyAssertionAxiom assertion : metaOntology.getDataPropertyAssertionAxioms(o)) {
	            if (assertion.getProperty().equals(ONTOLOGY_NAME_PROPERTY)) {
	                String name = assertion.getObject().getLiteral();
	                IRI ontologyName = IRI.create(name);
	                for (OWLOntology ontology : ontologies) {
	                    if (ontology.getOntologyID().getOntologyIRI().equals(ontologyName)) {
	                        ontologyMap.put((OWLAnonymousIndividual) o, ontology);
	                        break;
	                    }
	                }
	            }
	        }
	        for (OWLDataPropertyAssertionAxiom assertion : metaOntology.getDataPropertyAssertionAxioms(o)) {
	            if (assertion.getProperty().equals(ONTOLOGY_CURRENT_REVISION_PROPERTY)) {
	                int revision = Integer.parseInt(assertion.getObject().getLiteral());
	                OWLOntology ontology = ontologyMap.get(o);
	                currentRevisionMap.put(ontology.getOntologyID().getOntologyIRI(), revision);
	            }
	        }
	    }
	    return ontologyMap;
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
			else if (ontologyMap.get(ontologyRepresentative) == null) {
			    return null;
			}
			else if (isAdd) {
				return new AddAxiom(ontologyMap.get(ontologyRepresentative), getOWLAxiom());
			}
			else {
				return new RemoveAxiom(ontologyMap.get(ontologyRepresentative), getOWLAxiom());
			}
		}
		finally {
			clear();
		}
	}

	public Map<IRI, Integer> getCurrentRevisionMap() {
        return Collections.unmodifiableMap(currentRevisionMap);
    }
	
	@Override
	protected Set<OWLAnnotation> processAnnotations(Set<OWLAnnotation> axiomAnnotations) {
	    clear();
		axiomAnnotations = new HashSet<OWLAnnotation>(axiomAnnotations);
		if (!(isAdd = axiomAnnotations.remove(ADD_AXIOM_ANNOTATION))) {
			isRemove = axiomAnnotations.remove(REMOVE_AXIOM_ANNOTATION);
		}
		OWLAnnotation aboutAnnotation = null;
		for (OWLAnnotation annotation : axiomAnnotations) {
		    if (annotation.getProperty().equals(AXIOM_ABOUT)) {
		        aboutAnnotation = annotation;
		        break;
		    }
		}
		if (aboutAnnotation != null) {
		    axiomAnnotations.remove(aboutAnnotation);
		    ontologyRepresentative = (OWLAnonymousIndividual) aboutAnnotation.getValue();
		}
		return axiomAnnotations;
	}
	
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		super.visit(axiom);
		if (!isAdd && !isRemove) {
			return;
		}
		if (axiom.getSubject().equals(ontologyRepresentative) && axiom.getProperty().equals(ONTOLOGY_IMPORTS)) {
		    OWLOntology ontology = ontologyMap.get(ontologyRepresentative);
		    if (ontology == null) {
		        return;
		    }
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
		if (axiom.getSubject().equals(ontologyRepresentative) && axiom.getProperty().equals(ONTOLOGY_IMPORTS)) {
		    OWLOntology ontology = ontologyMap.get(ontologyRepresentative);
		    if (ontology == null) {
		        return;
		    }
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
