package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.CommitOption;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.protege.owl.server.api.exception.OWLServerException;
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
	
	
	public VersionedOWLOntology createServerOntology(IRI serverIRI, ChangeMetaData metaData, OWLOntology ontology) throws OWLServerException {
		RemoteOntologyDocument doc = client.createRemoteOntology(serverIRI);
		VersionedOWLOntology versionedOntology = factory.createVersionedOntology(ontology, doc, OntologyDocumentRevision.START_REVISION);
		commit(metaData, versionedOntology);
		update(versionedOntology);
		return versionedOntology;
	}

	public VersionedOWLOntology loadOntology(OWLOntologyManager manager, RemoteOntologyDocument doc) throws OWLOntologyCreationException, OWLServerException {
		return loadOntology(manager, doc, null);
	}
	
	public VersionedOWLOntology loadOntology(OWLOntologyManager manager, RemoteOntologyDocument doc, OntologyDocumentRevision revision) throws OWLOntologyCreationException, OWLServerException {
		ChangeDocument changes = client.getChanges(doc, OntologyDocumentRevision.START_REVISION, revision);
		OWLOntology ontology = manager.createOntology();		
		manager.applyChanges(changes.getChanges(ontology));
		VersionedOWLOntology versionedOntology = factory.createVersionedOntology(ontology, doc, changes.getEndRevision());
		versionedOntology.appendLocalHistory(changes);
		return versionedOntology;
	}
	
	public void commit(ChangeMetaData metaData, VersionedOWLOntology ontologyDoc) throws OWLServerException {
		RemoteOntologyDocument serverDoc = ontologyDoc.getServerDocument();
		OntologyDocumentRevision revision = ontologyDoc.getRevision();
		ChangeDocument serverHistory = getChanges(ontologyDoc, OntologyDocumentRevision.START_REVISION, revision);
        OWLOntology ontology = ontologyDoc.getOntology();
		List<OWLOntologyChange> baselineHistory = serverHistory.getChanges(ontology);
		for (ChangeDocument alreadyCommitted : ontologyDoc.getCommittedChanges()) {
		    baselineHistory.addAll(alreadyCommitted.getChanges(ontology));
		}
		List<OWLOntologyChange> uncommittedChanges = getUncommittedChanges(ontologyDoc.getOntology(), baselineHistory);
		List<ChangeDocument> myPreviousCommits = ontologyDoc.getCommittedChanges();
		ChangeDocument newCommit = client.commit(serverDoc, 
		                                         factory.createChangeDocument(uncommittedChanges, metaData, revision), collectCommitRevisions(myPreviousCommits),
		                                         CommitOption.RETURN_ACTUAL_COMMIT);
		myPreviousCommits.add(newCommit);
		ontologyDoc.setCommittedChanges(myPreviousCommits);
	}
	
	private SortedSet<OntologyDocumentRevision> collectCommitRevisions(List<ChangeDocument> previousCommits) {
	    SortedSet<OntologyDocumentRevision> revisions = new TreeSet<OntologyDocumentRevision>();
	    for (ChangeDocument previousCommit : previousCommits) {
	        for (OntologyDocumentRevision previousCommitRevision = previousCommit.getStartRevision();
	                previousCommitRevision.compareTo(previousCommit.getEndRevision()) < 0;
	                previousCommitRevision = previousCommitRevision.next()) {
	            revisions.add(previousCommitRevision);
	        }
	    }
	    return revisions;
	}
	
	public void update(VersionedOWLOntology ontology) throws OWLServerException {
		update(ontology, null);
	}
	
	public void update(VersionedOWLOntology openOntology, OntologyDocumentRevision revision) throws OWLServerException {
		OWLOntology localOntology = openOntology.getOntology();
		OWLOntologyManager manager = localOntology.getOWLOntologyManager();
		OntologyDocumentRevision startRevision = openOntology.getRevision();
		ChangeDocument updates = getChanges(openOntology, startRevision, revision);
		manager.applyChanges(updates.getChanges(localOntology));
		openOntology.setRevision(updates.getEndRevision());
		List<ChangeDocument> committedChanges = new ArrayList<ChangeDocument>();
		for (ChangeDocument committedChange : openOntology.getCommittedChanges()) {
		    if (committedChange.getEndRevision().compareTo(updates.getEndRevision()) > 0) {
		        committedChanges.add(committedChange.cropChanges(updates.getEndRevision(), null));
		    }
		}
		openOntology.setCommittedChanges(committedChanges);
	}
	
	public ChangeDocument getChanges(VersionedOWLOntology ontologyDoc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException {
		ChangeDocument localHistory = ontologyDoc.getLocalHistory();
		OntologyDocumentRevision realEnd = end;
		if (end == null || localHistory.getEndRevision().compareTo(end) < 0) {
			ChangeDocument newChanges = client.getChanges(ontologyDoc.getServerDocument(), localHistory.getEndRevision(), end);
			realEnd = newChanges.getEndRevision();
			ontologyDoc.appendLocalHistory(newChanges);
		}
		return ontologyDoc.getLocalHistory().cropChanges(start, realEnd);
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
			ontologyIdChanged = !ontologyId.isAnonymous();
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
