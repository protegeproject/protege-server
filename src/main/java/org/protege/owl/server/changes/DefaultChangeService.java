package org.protege.owl.server.changes;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.changes.api.ChangeHistory;

import java.io.File;
import java.io.FileNotFoundException;

public class DefaultChangeService implements ChangeService {

    private ChangeDocumentPool changePool;

    public DefaultChangeService(ChangeDocumentPool changePool) {
        this.changePool = changePool;
    }

    @Override
    public ChangeHistory getChanges(File resourceLocation, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws OWLServerException {
        HistoryFile historyFile = getHistoryFile(resourceLocation);
        return changePool.getChangeDocument(historyFile).cropChanges(startRevision, endRevision);
    }

    public ChangeHistory getAllChanges(File resourceLocation) throws OWLServerException {
        OntologyDocumentRevision headRevision = getHeadRevision(resourceLocation);
        return getChanges(resourceLocation, OntologyDocumentRevision.START_REVISION, headRevision);
    }

    public ChangeHistory getLatestChanges(File resourceLocation, OntologyDocumentRevision startRevision)
            throws OWLServerException {
        OntologyDocumentRevision headRevision = getHeadRevision(resourceLocation);
        return getChanges(resourceLocation, startRevision, headRevision);
    }

    protected OntologyDocumentRevision getHeadRevision(File resourceLocation) throws OWLServerException {
        HistoryFile historyFile = getHistoryFile(resourceLocation);
        return changePool.getChangeDocument(historyFile).getEndRevision();
    }

    private HistoryFile getHistoryFile(File resourceLocation) throws ServerRequestException {
        try {
            HistoryFile historyFile = new HistoryFile(resourceLocation.getPath());
            if (!historyFile.exists()) {
                throw new FileNotFoundException(); // TODO: Use factory to create the history file
            }
            return historyFile;
        }
        catch (FileNotFoundException e) {
            throw new ServerRequestException(e);
        }
    }
}
