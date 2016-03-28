package org.protege.owl.server.connect;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.api.ChangeHistory;
import org.protege.owl.server.changes.api.ServerOntologyDocument;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class RmiChangeService implements RemoteChangeService {

    public static String CHANGE_SERVICE = "RmiChangeService";

    private ChangeService changeService;

    public RmiChangeService(ChangeService changeService) {
        this.changeService = changeService;
    }

    @Override
    public ChangeHistory getChanges(ServerOntologyDocument doc, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception {
        try {
            return changeService.getChanges(doc, startRevision, endRevision);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<OWLOntologyChange> getConflicts(ServerOntologyDocument doc, CommitBundle commits) throws Exception {
        try {
            return changeService.getConflicts(doc, commits);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
