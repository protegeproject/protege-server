package org.protege.owl.server.changes;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.api.ChangeHistory;

public class DefaultChangeService implements ChangeService {

    private ChangeDocumentPool changePool;

    public DefaultChangeService(ChangeDocumentPool changePool) {
        this.changePool = changePool;
    }

    @Override
    public ChangeHistory getChanges(HistoryFile historyFile, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws OWLServerException {
        return changePool.getChangeDocument(historyFile).cropChanges(startRevision, endRevision);
    }

    @Override
    public ChangeHistory getAllChanges(HistoryFile historyFile) throws OWLServerException {
        OntologyDocumentRevision headRevision = getHeadRevision(historyFile);
        return getChanges(historyFile, OntologyDocumentRevision.START_REVISION, headRevision);
    }

    @Override
    public ChangeHistory getLatestChanges(HistoryFile historyFile, OntologyDocumentRevision startRevision)
            throws OWLServerException {
        OntologyDocumentRevision headRevision = getHeadRevision(historyFile);
        return getChanges(historyFile, startRevision, headRevision);
    }

    @Override
    public OntologyDocumentRevision getHeadRevision(HistoryFile historyFile) throws OWLServerException {
        return changePool.getChangeDocument(historyFile).getEndRevision();
    }
}
