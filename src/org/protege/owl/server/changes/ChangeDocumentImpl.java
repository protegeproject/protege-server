package org.protege.owl.server.changes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.OntologyDocument;
import org.protege.owl.server.api.ServerRevision;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.SetOntologyID;

public class ChangeDocumentImpl implements ChangeDocument {
	private OWLOntology changesOntology;
	private ServerRevision startRevision;

	@Override
	public OntologyDocument getOntologyDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerRevision getStartRevision() {
		return startRevision;
	}

	@Override
	public ServerRevision getEndRevision() {
		int revision = startRevision.getRevision() + changesOntology.getAxiomCount() + changesOntology.getAnnotations().size();
		return new ServerRevision(revision);
	}

	@Override
	public ChangeDocument cropChanges(ServerRevision start, ServerRevision end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
		OntologyToChangesUtil otcu = new OntologyToChangesUtil(ontology);
		otcu.handleAxioms();
		otcu.handleAnnotations();
		return otcu.getChanges();
	}
	
	
	private class OntologyToChangesUtil {
		private OWLOntology ontology;
		private Map<Integer, OWLOntologyChange> changeMap = new TreeMap<Integer, OWLOntologyChange>();
		
		
		private OntologyToChangesUtil(OWLOntology ontology) {
			this.ontology = ontology;
		}
		
		
		public void handleAxioms() {
			for (OWLAxiom axiom : changesOntology.getAxioms()) {
				Set<OWLAnnotation> annotations = axiom.getAnnotations();
				OWLAxiom cleanedAxiom = axiom.getAxiomWithoutAnnotations().getAnnotatedAxiom(removeChangeOntologyAnnotations(annotations));
				int revision = getRevision(annotations);
				OWLOntologyChange change;
				if (isAdded(annotations)) {
					change = new AddAxiom(ontology, cleanedAxiom);
				}
				else {
					change = new RemoveAxiom(ontology, cleanedAxiom);
				}
				changeMap.put(revision, change);
			}
		}
		
		public void handleAnnotations() {
			for (OWLAnnotation annotation : changesOntology.getAnnotations()) {
				OWLAnnotationValue rawValue = annotation.getValue();
				int revision = getRevision(annotation.getAnnotations());
				OWLOntologyChange change;
				if (annotation.getProperty().equals(ChangeOntology.SET_ONTOLOGY_ID)) {
					IRI name = (IRI) rawValue;
					IRI version = getVersionIRI(annotation.getAnnotations());
					OWLOntologyID id;
					if (version == null) {
						id = new OWLOntologyID(name);
					}
					else {
						id = new OWLOntologyID(name, version);
					}
					change = new SetOntologyID(ontology, id);
				}
			}
		}
		
		public List<OWLOntologyChange> getChanges() {
			List<OWLOntologyChange> changeList = new ArrayList<OWLOntologyChange>();
			OWLOntologyChange change;
			int revision = startRevision.getRevision();
			while ((change = changeMap.get(revision++)) != null) {
				changeList.add(change);
			}
			return changeList;
		}
		
		private int getRevision(Set<OWLAnnotation> annotations) {
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getProperty().equals(ChangeOntology.REVISION)) {
					return ((OWLLiteral) annotation.getValue()).parseInteger();
				}
			}
			throw new IllegalStateException("Revision information expected but not found.");
		}
		
		private boolean isAdded(Set<OWLAnnotation> annotations) {
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getProperty().equals(ChangeOntology.IS_AXIOM_ADDED)) {
					return ((OWLLiteral) annotation.getValue()).parseBoolean();
				}
			}
			throw new IllegalStateException("Added/Removed information expected but not found.");
		}

		private IRI getVersionIRI(Set<OWLAnnotation> annotations) {
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getProperty().equals(ChangeOntology.SET_ONTOLOGY_VERSION)) {
					return (IRI) annotation.getValue();
				}
			}
			return null;
		}
		
		private Set<OWLAnnotation> removeChangeOntologyAnnotations(Set<OWLAnnotation> annotations) {
			Set<OWLAnnotation> cleanedAnnotations = new TreeSet<OWLAnnotation>(annotations);
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getProperty().getIRI().toString().startsWith(ChangeOntology.NS)) {
					cleanedAnnotations.remove(annotation);
				}
			}
			return cleanedAnnotations;
		}

	}

	

}
