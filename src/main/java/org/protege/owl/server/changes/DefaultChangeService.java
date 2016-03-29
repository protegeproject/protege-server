package org.protege.owl.server.changes;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.changes.api.ChangeHistory;

import java.io.File;

public class DefaultChangeService implements ChangeService {

    private ChangeDocumentPool changePool;

    public DefaultChangeService(ChangeDocumentPool changePool) {
        this.changePool = changePool;
    }

    @Override
    public ChangeHistory getChanges(File historyFile, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception {
        return changePool.getChangeDocument(historyFile).cropChanges(startRevision, endRevision);
    }

    public ChangeHistory getChanges(File historyFile, OntologyDocumentRevision endRevision)
            throws Exception {
        return getChanges(historyFile, OntologyDocumentRevision.START_REVISION, endRevision);
    }
}
