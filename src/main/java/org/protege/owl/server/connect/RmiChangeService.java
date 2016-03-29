package org.protege.owl.server.connect;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.changes.HistoryFile;
import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.api.ChangeHistory;

import java.rmi.RemoteException;

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
    public ChangeHistory getChanges(HistoryFile historyFile, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception {
        try {
            return changeService.getChanges(historyFile, startRevision, endRevision);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
