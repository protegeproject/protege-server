package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.VersionedOntologyDocument;
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

	private ClientUtilities() {

	}

	/*
	 * Calls involving OWLOntology...
	 */
	
	
	public static void createServerOntology(Client client, IRI serverIRI, ChangeMetaData metaData, OWLOntology ontology) throws OWLServerException {
	    createServerOntologyInternal(client, serverIRI, metaData, ontology);
	}

	
	public static VersionedOntologyDocument createAndGetServerOntology(Client client, IRI serverIRI, ChangeMetaData metaData, OWLOntology ontology) throws OWLServerException {
	    VersionedOntologyDocument  versionedOntology = createServerOntologyInternal(client, serverIRI, metaData, ontology); 
	    update(client, versionedOntology);
	    return versionedOntology;
	}
	
	private static VersionedOntologyDocument createServerOntologyInternal(Client client, IRI serverIRI, ChangeMetaData metaData, OWLOntology ontology) throws OWLServerException {
        DocumentFactory factory = client.getDocumentFactory();
        RemoteOntologyDocument doc = client.createRemoteOntology(serverIRI);
        VersionedOntologyDocument versionedOntology = factory.createVersionedOntology(ontology, doc, OntologyDocumentRevision.START_REVISION);
        commit(client, metaData, versionedOntology);
        return versionedOntology;
	}

	public static VersionedOntologyDocument loadOntology(Client client, OWLOntologyManager manager, RemoteOntologyDocument doc) throws OWLOntologyCreationException, OWLServerException {
		return loadOntology(client, manager, doc, RevisionPointer.HEAD_REVISION);
	}
	
	public static VersionedOntologyDocument loadOntology(Client client, OWLOntologyManager manager, RemoteOntologyDocument doc, RevisionPointer revision) throws OWLOntologyCreationException, OWLServerException {
	    DocumentFactory factory = client.getDocumentFactory();
		ChangeHistory changes = client.getChanges(doc, OntologyDocumentRevision.START_REVISION.asPointer(), revision);
		OWLOntology ontology = manager.createOntology();		
		manager.applyChanges(changes.getChanges(ontology));
		VersionedOntologyDocument versionedOntology = factory.createVersionedOntology(ontology, doc, changes.getEndRevision());
		versionedOntology.appendLocalHistory(changes);
		return versionedOntology;
	}
	
	public static void commit(Client client, ChangeMetaData metaData, VersionedOntologyDocument ontologyDoc) throws OWLServerException {
        DocumentFactory factory = client.getDocumentFactory();
		RemoteOntologyDocument serverDoc = ontologyDoc.getServerDocument();
		OntologyDocumentRevision revision = ontologyDoc.getRevision();
		List<OWLOntologyChange> uncommittedChanges = getUncommittedChanges(client, ontologyDoc);
		client.commit(serverDoc, 
		              factory.createChangeDocument(uncommittedChanges, metaData, revision));
	}
	
	public static List<OWLOntologyChange> getUncommittedChanges(Client client, VersionedOntologyDocument ontologyDoc) throws OWLServerException {
	    OntologyDocumentRevision revision = ontologyDoc.getRevision();
	    ChangeHistory historyToClientRevision = getChanges(client, ontologyDoc, OntologyDocumentRevision.START_REVISION.asPointer(), revision.asPointer());
	    OWLOntology ontology = ontologyDoc.getOntology();
	    List<OWLOntologyChange> baselineHistory = historyToClientRevision.getChanges(ontology);
	    ChangeHistory historyFromClientRevision = getChanges(client, ontologyDoc, revision.asPointer(), RevisionPointer.HEAD_REVISION);
	    for (OntologyDocumentRevision possiblePastCommitRevision = historyFromClientRevision.getStartRevision();
	            possiblePastCommitRevision.compareTo(historyFromClientRevision.getEndRevision()) < 0;
	            possiblePastCommitRevision = possiblePastCommitRevision.next()) {
	        if (historyFromClientRevision.getMetaData(possiblePastCommitRevision).getUserId().equals(client.getUserId())) {
	            ChangeHistory pastCommit = historyFromClientRevision.cropChanges(possiblePastCommitRevision, possiblePastCommitRevision.next());
	            baselineHistory.addAll(pastCommit.getChanges(ontology));
	        }
	    }
	    return getUncommittedChanges(ontologyDoc.getOntology(), baselineHistory);
	}
	
	private static List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology, List<OWLOntologyChange> toBaseline) {
	    List<OWLOntologyChange> reversedList = new ArrayList<OWLOntologyChange>(toBaseline);
	    Collections.reverse(reversedList);
	    GetUncommittedChangesVisitor visitor = new GetUncommittedChangesVisitor(ontology);
	    for (OWLOntologyChange change : toBaseline) {
	        change.accept(visitor);
	    }
	    return visitor.getChanges();
	}
	
	public static void update(Client client, VersionedOntologyDocument ontology) throws OWLServerException {
		update(client, ontology, RevisionPointer.HEAD_REVISION);
	}
	
	public static void update(Client client, VersionedOntologyDocument openOntology, RevisionPointer targetRevisionPointer) throws OWLServerException {
		OWLOntology localOntology = openOntology.getOntology();
		OWLOntologyManager manager = localOntology.getOWLOntologyManager();
		OntologyDocumentRevision currentRevision = openOntology.getRevision();
		OntologyDocumentRevision targetRevision = client.evaluateRevisionPointer(openOntology.getServerDocument(), targetRevisionPointer);
		if (currentRevision.equals(targetRevision)) {
		    ;
		}
		else if (currentRevision.compareTo(targetRevision) < 0) {
		    ChangeHistory updates = getChanges(client, openOntology, currentRevision.asPointer(), targetRevisionPointer);
		    manager.applyChanges(updates.getChanges(localOntology));
		}
		else { // invert the changes
		    ChangeHistory baseline = getChanges(client, openOntology, OntologyDocumentRevision.START_REVISION.asPointer(), targetRevisionPointer);
            ChangeHistory updates = getChanges(client, openOntology, targetRevisionPointer, currentRevision.asPointer());	 
            manager.applyChanges(ChangeUtilities.invertChanges(baseline.getChanges(localOntology), updates.getChanges(localOntology)));
		}
		openOntology.setRevision(targetRevision);
	}
	
	public static ChangeHistory getChanges(Client client, VersionedOntologyDocument ontologyDoc, RevisionPointer start, RevisionPointer end) throws OWLServerException {
		if (start.isSymbolic() || end.isSymbolic() || end.asOntologyDocumentRevision().compareTo(ontologyDoc.getLocalHistory().getEndRevision()) > 0) {
	          ChangeHistory newChanges = client.getChanges(ontologyDoc.getServerDocument(), start, end);
	          OntologyDocumentRevision realStart = newChanges.getStartRevision();
	          if (ontologyDoc.getLocalHistory().getEndRevision().compareTo(realStart) >= 0) {
	            ontologyDoc.appendLocalHistory(newChanges);
	          }
	          return newChanges;
		}
		OntologyDocumentRevision realStart = start.asOntologyDocumentRevision();
		OntologyDocumentRevision realEnd   = end.asOntologyDocumentRevision();
		return ontologyDoc.getLocalHistory().cropChanges(realStart, realEnd);
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
	
	private static class GetUncommittedChangesVisitor implements OWLOntologyChangeVisitor {
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
