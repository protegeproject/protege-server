package org.protege.editor.owl.server.transport.rmi;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface RemoteChangeService extends Remote {

    ChangeHistory getChanges(HistoryFile historyFile, DocumentRevision startRevision, DocumentRevision endRevision)
            throws RemoteException;

    ChangeHistory getLatestChanges(HistoryFile historyFile, DocumentRevision startRevision) throws RemoteException;

    ChangeHistory getAllChanges(HistoryFile historyFile) throws RemoteException;

    DocumentRevision getHeadRevision(HistoryFile historyFile) throws RemoteException;
}
