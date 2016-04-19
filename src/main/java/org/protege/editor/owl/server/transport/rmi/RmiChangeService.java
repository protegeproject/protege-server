package org.protege.editor.owl.server.transport.rmi;

import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.versioning.DocumentRevision;
import org.protege.editor.owl.server.versioning.HistoryFile;
import org.protege.editor.owl.server.versioning.ServerDocument;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;

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
    public ChangeHistory getChanges(HistoryFile historyFile,
            DocumentRevision startRevision,
            DocumentRevision endRevision) throws RemoteException {
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
            DocumentRevision startRevision,
            DocumentRevision endRevision) throws RemoteException {
        return getChanges(serverDocument.getHistoryFile(), startRevision, endRevision);
    }

    @Override
    public ChangeHistory getLatestChanges(HistoryFile historyFile,
            DocumentRevision startRevision) throws RemoteException {
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
            DocumentRevision startRevision) throws RemoteException {
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
    public DocumentRevision getHeadRevision(HistoryFile historyFile) throws RemoteException {
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
    public DocumentRevision getHeadRevision(ServerDocument serverDocument) throws RemoteException {
        return getHeadRevision(serverDocument.getHistoryFile());
    }
}
