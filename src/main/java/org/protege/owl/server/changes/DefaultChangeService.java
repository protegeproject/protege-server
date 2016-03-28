package org.protege.owl.server.changes;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.server.ServerPath;
import org.protege.owl.server.changes.api.ChangeHistory;
import org.protege.owl.server.changes.api.ServerOntologyDocument;
import org.protege.owl.server.changes.util.CollectingChangeVisitor;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class DefaultChangeService implements ChangeService {

    private ChangeDocumentPool changePool;

    public DefaultChangeService(ChangeDocumentPool changePool) {
        this.changePool = changePool;
    }

    @Override
    public ChangeHistory getChanges(ServerOntologyDocument doc, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception {
        File historyFile = getChangeHistoryFile(doc.getServerPath());
        return changePool.getChangeDocument(doc, historyFile).cropChanges(startRevision, endRevision);
    }

    @Override
    public List<OWLOntologyChange> getConflicts(ServerOntologyDocument doc, CommitBundle commits) throws Exception {
        List<OWLOntologyChange> conflicts = new ArrayList<OWLOntologyChange>();
        OWLOntology cacheOntology = OWLManager.createOWLOntologyManager().createOntology();
        
        List<OWLOntologyChange> clientChanges = commits.getChanges();
        CollectingChangeVisitor collectedClientChanges = CollectingChangeVisitor.collectChanges(clientChanges);
        
        OntologyDocumentRevision headRevision = getHeadRevision(doc);
        ChangeHistory fullChangeHistory = getChanges(doc, OntologyDocumentRevision.START_REVISION, headRevision);
        OntologyDocumentRevision revision = commits.getRevision();
        for (; revision.compareTo(headRevision) < 0; revision = revision.next()) {
            List<OWLOntologyChange> serverChanges = fullChangeHistory.cropChanges(revision, revision.next()).getChanges(cacheOntology);
            CollectingChangeVisitor collectedServerChanges = CollectingChangeVisitor.collectChanges(serverChanges);
            computeConflicts(collectedClientChanges, collectedServerChanges, conflicts);
        }
        return conflicts;
    }

    private void computeConflicts(CollectingChangeVisitor clientChanges, CollectingChangeVisitor serverChanges, List<OWLOntologyChange> conflicts) {
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

    private File getChangeHistoryFile(ServerPath serverPath) throws Exception {
        return new File("<History file>"); // TODO: Visit this later; handle if file not found
    }

    private OntologyDocumentRevision getHeadRevision(ServerOntologyDocument doc) throws Exception {
        File historyFile = getChangeHistoryFile(doc.getServerPath());
        return changePool.getChangeDocument(doc, historyFile).getEndRevision();
    }
}
