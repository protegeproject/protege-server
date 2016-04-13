package org.protege.owl.server.connect;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.changes.HistoryFile;
import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.ServerDocument;
import org.protege.owl.server.changes.api.ChangeHistory;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class RmiChangeService implements RemoteChangeService, Remote {

    public static String CHANGE_SERVICE = "RmiChangeService";

    private ChangeService changeService;

    public RmiChangeService(ChangeService changeService) {
        this.changeService = changeService;
    }

    @Override
    public ChangeHistory getChanges(HistoryFile historyFile,
            OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws RemoteException {
        try {
            return changeService.getChanges(historyFile, startRevision, endRevision);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * A helper method to calculate changes given the {@code ServerDocument} as input.
     */
    public ChangeHistory getChanges(ServerDocument serverDocument,
            OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws RemoteException {
        return getChanges(serverDocument.getHistoryFile(), startRevision, endRevision);
    }

    @Override
    public ChangeHistory getLatestChanges(HistoryFile historyFile,
            OntologyDocumentRevision startRevision) throws RemoteException {
        try {
            return changeService.getLatestChanges(historyFile, startRevision);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * A helper method to calculate the latest changes given the {@code ServerDocument} as input.
     */
    public ChangeHistory getLatestChanges(ServerDocument serverDocument,
            OntologyDocumentRevision startRevision) throws RemoteException {
        return getLatestChanges(serverDocument.getHistoryFile(), startRevision);
    }

    @Override
    public ChangeHistory getAllChanges(HistoryFile historyFile) throws RemoteException {
        try {
            return changeService.getAllChanges(historyFile);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * A helper method to calculate the latest changes given the {@code ServerDocument} as input.
     */
    public ChangeHistory getAllChanges(ServerDocument serverDocument) throws RemoteException {
        return getAllChanges(serverDocument.getHistoryFile());
    }

    @Override
    public OntologyDocumentRevision getHeadRevision(HistoryFile historyFile) throws RemoteException {
        try {
            return changeService.getHeadRevision(historyFile);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /**
     * A helper method to get the remote head revision given the {@code ServerDocument} as input.
     */
    public OntologyDocumentRevision getHeadRevision(ServerDocument serverDocument) throws RemoteException {
        return getHeadRevision(serverDocument.getHistoryFile());
    }
}
