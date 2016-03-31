package org.protege.owl.server.changes;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.ServerFilterAdapter;
import org.protege.owl.server.api.ServerLayer;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.api.server.TransportHandler;
import org.protege.owl.server.changes.api.ChangeHistory;
import org.protege.owl.server.changes.util.CollectingChangeVisitor;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Project;

/**
 * Represents the change document layer that will validate the user changes in the commit document.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ConflictDetectionFilter extends ServerFilterAdapter {

    private ChangeDocumentPool changePool = new ChangeDocumentPool();

    private DefaultChangeService changeService;

    public ConflictDetectionFilter(ServerLayer delegate) {
        super(delegate);
        changeService = new DefaultChangeService(changePool);
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle commits) throws ServerRequestException {
       try {
            List<OWLOntologyChange> conflicts = getConflicts(project, commits);
            if (!conflicts.isEmpty()) {
                throw new ServerRequestException("Conflicts detected: " + conflicts); // TODO: Fix the exception
            }
            super.commit(token, project, commits);
       }
       catch (OWLOntologyCreationException e) {
           throw new ServerRequestException(e);
       }
       catch (OWLServerException e) {
           throw new ServerRequestException(e);
       }
    }

    private List<OWLOntologyChange> getConflicts(Project project, CommitBundle commits) throws OWLOntologyCreationException, OWLServerException {
        List<OWLOntologyChange> conflicts = new ArrayList<OWLOntologyChange>();
        OWLOntology cacheOntology = OWLManager.createOWLOntologyManager().createOntology();
        
        List<OWLOntologyChange> clientChanges = commits.getChanges();
        CollectingChangeVisitor collectedClientChanges = CollectingChangeVisitor.collectChanges(clientChanges);
        
        ChangeHistory allChangeHistory = changeService.getAllChanges(project.getFile());
        
        final OntologyDocumentRevision headRevision = changeService.getHeadRevision(project.getFile());
        OntologyDocumentRevision revision = commits.getStartRevision();
        for (; revision.compareTo(headRevision) < 0; revision = revision.next()) {
            ChangeHistory singleRevisionChangeHistory = allChangeHistory.cropChanges(revision, revision.next());
            List<OWLOntologyChange> serverChanges = singleRevisionChangeHistory.getChanges(cacheOntology);
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

    @Override
    public void setTransport(TransportHandler transport) throws OWLServerException {
        try {
            transport.bind(changeService);
        }
        catch (Exception e) {
            throw new OWLServerException(e);
        }
        super.setTransport(transport);
    }
}
