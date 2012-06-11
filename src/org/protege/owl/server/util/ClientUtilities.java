package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.OntologyDocument;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class ClientUtilities {
	private Client client;
	private DocumentFactory factory;

	public ClientUtilities(Client client) {
		this.client = client;
		factory = client.getDocumentFactory();
	}

	/*
	 * Calls involving OWLOntology...
	 */
	
	public OntologyDocument getOntologyDocument(OWLOntology ontology) {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		IRI source = manager.getOntologyDocumentIRI(ontology);
		return client.getDocumentFactory().loadOntologyDocument(source);
	}
	
	public OWLOntology loadOntology(OWLOntologyManager manager, OntologyDocument doc) throws OWLOntologyCreationException {
		ChangeDocument changes = client.getChanges(doc, OntologyDocumentRevision.START_REVISION, null);
		OWLOntology ontology = manager.createOntology();		
		manager.applyChanges(changes.getChanges(ontology));
		return ontology;
	}
	
	public OWLOntology loadOntology(OWLOntologyManager manager, OntologyDocument doc, OntologyDocumentRevision revision) throws OWLOntologyCreationException {
		ChangeDocument changes = client.getChanges(doc, OntologyDocumentRevision.START_REVISION, revision);
		OWLOntology ontology = manager.createOntology();		
		manager.applyChanges(changes.getChanges(ontology));
		return ontology;
	}
	
	public void commit(String commitComment, OWLOntology ontology) {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		IRI ontologyIRI = manager.getOntologyDocumentIRI(ontology);
		OntologyDocument doc = factory.loadOntologyDocument(ontologyIRI);
		OntologyDocumentRevision revision = doc.getCurrentRevision();
		List<OWLOntologyChange> changes = getUncommittedChanges(ontology, doc, revision);
		client.commit(doc, revision, factory.createChangeDocument(changes, revision));
	}
	
	public void update(OWLOntology ontology) {
		update(ontology, null);
	}
	
	public void update(OWLOntology ontology, OntologyDocumentRevision revision) {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		IRI ontologyIRI = manager.getOntologyDocumentIRI(ontology);
		OntologyDocument doc = factory.loadOntologyDocument(ontologyIRI);
		OntologyDocumentRevision startRevision = doc.getCurrentRevision();
		ChangeDocument changes = client.getChanges(doc, startRevision, revision);
		manager.applyChanges(changes.getChanges(ontology));
	}
	
	public List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology) {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		IRI ontologyIri = manager.getOntologyDocumentIRI(ontology);
		OntologyDocument doc = factory.loadOntologyDocument(ontologyIri);
		OntologyDocumentRevision revision = doc.getCurrentRevision();
		return getUncommittedChanges(ontology, doc, revision);
	}
	
	private List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology, OntologyDocument doc, OntologyDocumentRevision revision) {
		List<OWLOntologyChange> baselineChanges = client.getChanges(doc, OntologyDocumentRevision.START_REVISION, revision).getChanges(ontology);
		GetUncommittedChangesVisitor visitor = new GetUncommittedChangesVisitor(ontology);
		for (OWLOntologyChange change : baselineChanges) {
			change.accept(visitor);
		}
		return visitor.getChanges();
	}
	
	
	/*
	 * This enum characterizes the difference the elements of two sets (e.g. of axioms) that are being compared.
	 * The sets are called the baseline set and the revised set.
	 */
	private enum OntologyChangeType {
		ADDED,       /* not found in the baseline set but present in the revised set */
		REMOVED,     /* found in the baseline set but not found in the revised set */
		ON_BOTH,     /* found in the baseline and the revised set */
		ON_NEITHER;  /* not found on either the baseline or the revised set */
		
		public static <X> void addElementToBaseline(X element, Map<X, OntologyChangeType> changes) {
			OntologyChangeType changeType = changes.get(element);
			if (changeType == null) {
				changes.put(element, OntologyChangeType.REMOVED);
			}
			else {
				switch (changeType) {
				case ADDED:
					changes.put(element, OntologyChangeType.ON_BOTH);
					break;
				case ON_NEITHER:
					changes.put(element, OntologyChangeType.REMOVED);
					break;
				case ON_BOTH:
				case REMOVED:
					break;
				}
			}
		}
		
		public static <X> void removeElementFromBaseline(X element, Map<X, OntologyChangeType> changes) {
			OntologyChangeType changeType = changes.get(element);
			if (changeType != null) {
				switch (changeType) {
				case REMOVED:
					changes.put(element, OntologyChangeType.ON_NEITHER);
					break;
				case ON_BOTH:
					changes.put(element, OntologyChangeType.ADDED);
					break;
				case ADDED:
				case ON_NEITHER:
					break;
				}
			}
		}
	}
	
	private class GetUncommittedChangesVisitor implements OWLOntologyChangeVisitor {
		private OWLOntology ontology;
		private OWLOntologyID ontologyId;
		private boolean ontologyIdChanged;
		private Map<OWLImportsDeclaration, OntologyChangeType> importChanges = new TreeMap<OWLImportsDeclaration, OntologyChangeType>();
		private Map<OWLAnnotation, OntologyChangeType> annotationChanges = new TreeMap<OWLAnnotation, OntologyChangeType>();
		private Map<OWLAxiom, OntologyChangeType> axiomChanges = new HashMap<OWLAxiom, OntologyChangeType>();
		
		
		public GetUncommittedChangesVisitor(OWLOntology ontology) {
			this.ontology = ontology;
			this.ontologyId = ontology.getOntologyID();
			ontologyIdChanged = true;
			for (OWLImportsDeclaration decl : ontology.getImportsDeclarations()) {
				importChanges.put(decl, OntologyChangeType.ADDED);
			}
			for (OWLAnnotation annotation : ontology.getAnnotations()) {
				annotationChanges.put(annotation, OntologyChangeType.ADDED);
			}
			for (OWLAxiom axiom : ontology.getAxioms()) {
				axiomChanges.put(axiom, OntologyChangeType.ADDED);
			}
		}
		
		public List<OWLOntologyChange> getChanges() {
			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
			if (ontologyIdChanged) {
				changes.add(new SetOntologyID(ontology, ontologyId));
			}
			for (Entry<OWLImportsDeclaration, OntologyChangeType> entry : importChanges.entrySet()) {
				OWLImportsDeclaration importDecl = entry.getKey();
				OntologyChangeType changeType = entry.getValue();
				if (changeType == OntologyChangeType.ADDED) {
					changes.add(new AddImport(ontology, importDecl));
				}
				else if (changeType == OntologyChangeType.REMOVED) {
					changes.add(new RemoveImport(ontology, importDecl));
				}
			}
			for (Entry<OWLAnnotation, OntologyChangeType> entry : annotationChanges.entrySet()) {
				OWLAnnotation annotation = entry.getKey();
				OntologyChangeType changeType = entry.getValue();
				if (changeType == OntologyChangeType.ADDED) {
					changes.add(new AddOntologyAnnotation(ontology, annotation));
				}
				else if (changeType == OntologyChangeType.REMOVED) {
					changes.add(new RemoveOntologyAnnotation(ontology, annotation));
				}
			}
			for (Entry<OWLAxiom, OntologyChangeType> entry : axiomChanges.entrySet()) {
				OWLAxiom axiom = entry.getKey();
				OntologyChangeType changeType = entry.getValue();
				if (changeType == OntologyChangeType.ADDED) {
					changes.add(new AddAxiom(ontology, axiom));
				}
				else if (changeType == OntologyChangeType.REMOVED) {
					changes.add(new RemoveAxiom(ontology, axiom));
				}
			}
			return changes;
		}

		@Override
		public void visit(AddAxiom change) {
			OWLAxiom axiom = change.getAxiom();
			OntologyChangeType.addElementToBaseline(axiom, axiomChanges);
		}

		@Override
		public void visit(RemoveAxiom change) {
			OWLAxiom axiom = change.getAxiom();
			OntologyChangeType.removeElementFromBaseline(axiom, axiomChanges);
		}

		@Override
		public void visit(SetOntologyID change) {
			OWLOntologyID id = change.getNewOntologyID();
			ontologyIdChanged = !id.equals(ontologyId);
		}

		@Override
		public void visit(AddImport change) {
			OWLImportsDeclaration importDecl = change.getImportDeclaration();
			OntologyChangeType.addElementToBaseline(importDecl, importChanges);
		}

		@Override
		public void visit(RemoveImport change) {
			OWLImportsDeclaration importDecl = change.getImportDeclaration();
			OntologyChangeType.removeElementFromBaseline(importDecl, importChanges);
		}

		@Override
		public void visit(AddOntologyAnnotation change) {
			OWLAnnotation annotation = change.getAnnotation();
			OntologyChangeType.addElementToBaseline(annotation, annotationChanges);
		}

		@Override
		public void visit(RemoveOntologyAnnotation change) {
			OWLAnnotation annotation = change.getAnnotation();
			OntologyChangeType.removeElementFromBaseline(annotation, annotationChanges);
		}
	}

}
