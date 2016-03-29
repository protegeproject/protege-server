package org.protege.owl.server.changes;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.changes.api.ChangeHistory;

public class DefaultChangeService implements ChangeService {

    private ChangeDocumentPool changePool;

    public DefaultChangeService(ChangeDocumentPool changePool) {
        this.changePool = changePool;
    }

    @Override
    public ChangeHistory getChanges(HistoryFile historyFile, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception {
        return changePool.getChangeDocument(historyFile).cropChanges(startRevision, endRevision);
    }

    public ChangeHistory getChanges(HistoryFile historyFile, OntologyDocumentRevision endRevision)
            throws Exception {
        return getChanges(historyFile, OntologyDocumentRevision.START_REVISION, endRevision);
    }
}
