package org.protege.owl.server.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.DocumentNotFoundException;
import org.protege.owl.server.impl.RemoteOntologyDocumentImpl;
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
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
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
	
	public VersionedOntologyDocument getOntologyDocument(OWLOntology ontology) throws IOException {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		IRI source = manager.getOntologyDocumentIRI(ontology);
		return client.getDocumentFactory().getSavedOntologyDocument(source);
	}
	
	public OWLOntology loadOntology(OWLOntologyManager manager, RemoteOntologyDocument doc) throws OWLOntologyCreationException, IOException {
		ChangeDocument changes = client.getChanges(doc, OntologyDocumentRevision.START_REVISION, null);
		OWLOntology ontology = manager.createOntology();		
		manager.applyChanges(changes.getChanges(ontology));
		return ontology;
	}
	
	public OWLOntology loadOntology(OWLOntologyManager manager, RemoteOntologyDocument doc, OntologyDocumentRevision revision) throws OWLOntologyCreationException, IOException {
		ChangeDocument changes = client.getChanges(doc, OntologyDocumentRevision.START_REVISION, revision);
		OWLOntology ontology = manager.createOntology();		
		manager.applyChanges(changes.getChanges(ontology));
		return ontology;
	}
	
	public void commit(String commitComment, VersionedOWLOntology ontologyDoc) throws IOException {
		RemoteOntologyDocument serverDoc = ontologyDoc.getServerDocument();
		OntologyDocumentRevision revision = serverDoc.getRevision();
		ChangeDocument baseLineChanges = client.getChanges(serverDoc, OntologyDocumentRevision.START_REVISION, revision);
		OWLOntology ontology = ontologyDoc.getOntology();
		List<OWLOntologyChange> changes = getUncommittedChanges(ontologyDoc.getOntology(), baseLineChanges.getChanges(ontology));
		Map<OntologyDocumentRevision, String> commitComments = Collections.singletonMap(revision, commitComment);
		client.commit(serverDoc, commitComment, factory.createChangeDocument(changes, commitComments, revision));
	}
	
	public void update(VersionedOWLOntology ontology) throws IOException {
		update(ontology, null);
	}
	
	public void update(VersionedOWLOntology openOntology, OntologyDocumentRevision revision) throws IOException {
		RemoteOntologyDocument backingStore = openOntology.getServerDocument();
		OWLOntology localOntology = openOntology.getOntology();
		OWLOntologyManager manager = localOntology.getOWLOntologyManager();
		OntologyDocumentRevision startRevision = backingStore.getRevision();
		ChangeDocument changes = client.getChanges(backingStore, startRevision, revision);
		manager.applyChanges(changes.getChanges(localOntology));
		backingStore.setRevision(revision);
	}
	
	
	private List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology, List<OWLOntologyChange> toBaseline) {
		List<OWLOntologyChange> reversedList = new ArrayList<OWLOntologyChange>(toBaseline);
		Collections.reverse(reversedList);
		GetUncommittedChangesVisitor visitor = new GetUncommittedChangesVisitor(ontology);
		for (OWLOntologyChange change : toBaseline) {
			change.accept(visitor);
		}
		return visitor.getChanges();
	}
	
	public void save(VersionedOWLOntology openOntology) throws IOException, OWLOntologyStorageException {
		OWLOntology ontology = openOntology.getOntology();
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		IRI savedLocation = manager.getOntologyDocumentIRI(ontology);
		VersionedOntologyDocument saved;
		try {
			saved = factory.getSavedOntologyDocument(savedLocation);
		}
		catch (DocumentNotFoundException e) {
			saved = factory.createSavedOntologyDocument(savedLocation, openOntology.getServerDocument());
		}
		manager.saveOntology(ontology);
		saved.getServerDocument().setRevision(openOntology.getServerDocument().getRevision());
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
