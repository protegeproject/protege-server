package org.protege.owl.server.changes;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.changes.api.ChangeHistory;

import java.io.FileNotFoundException;

import edu.stanford.protege.metaproject.api.Address;

public class DefaultChangeService implements ChangeService {

    private ChangeDocumentPool changePool;

    public DefaultChangeService(ChangeDocumentPool changePool) {
        this.changePool = changePool;
    }

    @Override
    public ChangeHistory getChanges(Address fileLocation, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws OWLServerException {
        HistoryFile historyFile = getHistoryFile(fileLocation);
        return changePool.getChangeDocument(historyFile).cropChanges(startRevision, endRevision);
    }

    public ChangeHistory getAllChanges(Address fileLocation) throws OWLServerException {
        OntologyDocumentRevision headRevision = getHeadRevision(fileLocation);
        return getChanges(fileLocation, OntologyDocumentRevision.START_REVISION, headRevision);
    }

    public ChangeHistory getLatestChanges(Address fileLocation, OntologyDocumentRevision startRevision)
            throws OWLServerException {
        OntologyDocumentRevision headRevision = getHeadRevision(fileLocation);
        return getChanges(fileLocation, startRevision, headRevision);
    }

    protected OntologyDocumentRevision getHeadRevision(Address fileLocation) throws OWLServerException {
        HistoryFile historyFile = getHistoryFile(fileLocation);
        return changePool.getChangeDocument(historyFile).getEndRevision();
    }

    private HistoryFile getHistoryFile(Address fileLocation) throws ServerRequestException {
        try {
            HistoryFile historyFile = new HistoryFile(fileLocation.get());
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
