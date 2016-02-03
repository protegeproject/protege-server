package org.protege.owl.server.conflict;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.SingletonChangeHistory;
import org.protege.owl.server.api.exception.ConflictException;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerOntologyDocument;
import org.protege.owl.server.api.server.ServerTransport;
import org.protege.owl.server.util.CollectingChangeVisitor;
import org.protege.owl.server.util.ServerFilterAdapter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ConflictManager extends ServerFilterAdapter {
    private Logger logger = LoggerFactory.getLogger(ConflictManager.class.getCanonicalName());

    public ConflictManager(Server delegate) {
        super(delegate);
    }
    
    @Override
    public void commit(AuthToken u, ServerOntologyDocument doc, SingletonChangeHistory proposedChanges) throws OWLServerException {
        List<OWLOntologyChange> conflicts = getConflicts(u, doc, proposedChanges);
        if (!conflicts.isEmpty()) {
            ChangeHistory history = getDocumentFactory().createChangeDocument(conflicts, new ChangeMetaData("Conflicts Found"), proposedChanges.getStartRevision());
            throw new ConflictException(history);
        }
        super.commit(u, doc, proposedChanges);
    }
    
    private List<OWLOntologyChange> getConflicts(AuthToken u, ServerOntologyDocument doc, ChangeHistory proposedChanges) throws OWLServerException {
        List<OWLOntologyChange> conflicts = new ArrayList<OWLOntologyChange>();
        OWLOntology fakeOntology;
        try {
            fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
        }
        catch (OWLOntologyCreationException ooce) {
            throw new RuntimeException("Could not create empty ontology", ooce);
        }
        List<OWLOntologyChange> clientChanges = proposedChanges.getChanges(fakeOntology);
        CollectingChangeVisitor collectedClientChanges = CollectingChangeVisitor.collectChanges(clientChanges);
        OntologyDocumentRevision head = super.evaluateRevisionPointer(u, doc, RevisionPointer.HEAD_REVISION);
        ChangeHistory fullServerHistory = getChanges(u, doc, OntologyDocumentRevision.START_REVISION, head);
        for (OntologyDocumentRevision revision = proposedChanges.getStartRevision();
                revision.compareTo(head) < 0;
                revision = revision.next()) {
            List<OWLOntologyChange> serverChanges = fullServerHistory.cropChanges(revision, revision.next()).getChanges(fakeOntology);
            CollectingChangeVisitor collectedServerChanges = CollectingChangeVisitor.collectChanges(serverChanges);
            addConflicts(collectedClientChanges, collectedServerChanges, conflicts);
        }
        return conflicts;
    }
    
    private void addConflicts(CollectingChangeVisitor clientChanges, CollectingChangeVisitor serverChanges, List<OWLOntologyChange> conflicts) {
        if (clientChanges.getLastOntologyIDChange() != null && serverChanges.getLastOntologyIDChange() != null) {
            conflicts.add(clientChanges.getLastOntologyIDChange());
        }
        for (Entry<OWLImportsDeclaration, ImportChange> entry : clientChanges.getLastImportChangeMap().entrySet()) {
            OWLImportsDeclaration decl = entry.getKey();
            if (serverChanges.getLastImportChangeMap().containsKey(decl)) {
                conflicts.add(entry.getValue());
            }
        }
        for (Entry<OWLAnnotation, OWLOntologyChange> entry : clientChanges.getLastOntologyAnnotationChangeMap().entrySet()) {
            OWLAnnotation annotation = entry.getKey();
            if (serverChanges.getLastOntologyAnnotationChangeMap().containsKey(annotation)) {
                conflicts.add(entry.getValue());
            }
        }
        for (Entry<OWLAxiom, OWLAxiomChange> entry : clientChanges.getLastAxiomChangeMap().entrySet()) {
            OWLAxiom axiom = entry.getKey();
            if (serverChanges.getLastAxiomChangeMap().containsKey(axiom)) {
                conflicts.add(entry.getValue());
            }
        }
    }
    
    @Override
    public void setTransports(Collection<ServerTransport> transports) {
        logger.info("Basic Conflict Management started.");
        super.setTransports(transports);
    }
    


}
