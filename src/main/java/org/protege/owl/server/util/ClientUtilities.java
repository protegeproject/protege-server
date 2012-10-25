package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.UserId;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
	    ChangeHistory historyAfterClientRevision = getChanges(client, ontologyDoc, revision.asPointer(), RevisionPointer.HEAD_REVISION);
	    OWLOntology ontology = ontologyDoc.getOntology();
	    List<OWLOntologyChange> baselineHistory = historyToClientRevision.getChanges(ontology);
	    return getUncommittedChanges(ontologyDoc.getOntology(), baselineHistory, client.getUserId(), historyAfterClientRevision);
	}
	
	private static List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology, List<OWLOntologyChange> toBaseline,
	                                                               UserId user, ChangeHistory historyAfterClientRevision) {
	    List<OWLOntologyChange> reversedList = new ArrayList<OWLOntologyChange>(toBaseline);
	    Collections.reverse(reversedList);
	    GetUncommittedChangesVisitor visitor = new GetUncommittedChangesVisitor(ontology);
	    for (OWLOntologyChange change : toBaseline) {
	        change.accept(visitor);
	    }
	    List<OWLOntologyChange> uncommitted = visitor.getChanges();
	    removeFutureCommits(uncommitted, ontology, user, historyAfterClientRevision);
	    return uncommitted;
	}
	
	private static void removeFutureCommits(List<OWLOntologyChange> uncommitted, OWLOntology ontology, UserId user, ChangeHistory historyAfterClientRevision) {
	    for (OntologyDocumentRevision revision = historyAfterClientRevision.getStartRevision();
	            revision.compareTo(historyAfterClientRevision.getEndRevision()) < 0;
	            revision = revision.next()) {
	        if (historyAfterClientRevision.getMetaData(revision).getUserId().equals(user)) {
	            List<OWLOntologyChange> previousCommit = historyAfterClientRevision.cropChanges(revision, revision.next()).getChanges(ontology);
	            uncommitted.removeAll(previousCommit);
	        }
	    }
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
	    OntologyDocumentRevision realStart = client.evaluateRevisionPointer(ontologyDoc.getServerDocument(), start);
	    OntologyDocumentRevision realEnd   = client.evaluateRevisionPointer(ontologyDoc.getServerDocument(), end);
		if (realEnd.compareTo(ontologyDoc.getLocalHistory().getEndRevision()) > 0) {
	          ChangeHistory newChanges = client.getChanges(ontologyDoc.getServerDocument(), ontologyDoc.getLocalHistory().getEndRevision().asPointer(), realEnd.asPointer());
	          ontologyDoc.appendLocalHistory(newChanges);
		}
		return ontologyDoc.getLocalHistory().cropChanges(realStart, realEnd);
	}

}
