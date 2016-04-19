package org.protege.owl.server.versioning;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.versioning.api.ChangeHistory;

public class DefaultChangeService implements ChangeService {

    private ChangeDocumentPool changePool;

    public DefaultChangeService(ChangeDocumentPool changePool) {
        this.changePool = changePool;
    }

    @Override
    public ChangeHistory getChanges(HistoryFile historyFile, DocumentRevision startRevision,
            DocumentRevision endRevision) throws OWLServerException {
        return changePool.getChangeDocument(historyFile).cropChanges(startRevision, endRevision);
    }

    @Override
    public ChangeHistory getAllChanges(HistoryFile historyFile) throws OWLServerException {
        DocumentRevision headRevision = getHeadRevision(historyFile);
        return getChanges(historyFile, DocumentRevision.START_REVISION, headRevision);
    }

    @Override
    public ChangeHistory getLatestChanges(HistoryFile historyFile, DocumentRevision startRevision)
            throws OWLServerException {
        DocumentRevision headRevision = getHeadRevision(historyFile);
        return getChanges(historyFile, startRevision, headRevision);
    }

    @Override
    public DocumentRevision getHeadRevision(HistoryFile historyFile) throws OWLServerException {
        return changePool.getChangeDocument(historyFile).getEndRevision();
    }
}
