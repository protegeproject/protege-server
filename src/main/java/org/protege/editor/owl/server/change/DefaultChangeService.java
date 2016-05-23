package org.protege.editor.owl.server.change;

import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.ChangeHistoryUtils;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;

import java.io.IOException;

public class DefaultChangeService implements ChangeService {

    private ChangeDocumentPool changePool;

    public DefaultChangeService(ChangeDocumentPool changePool) {
        this.changePool = changePool;
    }

    @Override
    public ChangeHistory getChanges(HistoryFile historyFile, DocumentRevision startRevision,
            DocumentRevision endRevision) throws ServerServiceException {
        ChangeHistory changeHistory = getCachedChangeHistory(historyFile);
        return ChangeHistoryUtils.crop(changeHistory, startRevision, endRevision);
    }

    @Override
    public DocumentRevision getHeadRevision(HistoryFile historyFile) throws ServerServiceException {
        return getCachedChangeHistory(historyFile).getHeadRevision();
    }

    private ChangeHistory getCachedChangeHistory(HistoryFile historyFile) throws ServerServiceException {
        try {
            return changePool.lookup(historyFile);
        }
        catch (IOException e) {
            throw new ServerServiceException("Error while getting the change history at the server", e);
        }
    }
}
