package org.protege.owl.server.connect;

import org.protege.owl.server.api.ChangeService;
import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.api.ChangeHistory;

import java.io.File;
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
    public ChangeHistory getChanges(File resourceLocation, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception {
        try {
            return changeService.getChanges(resourceLocation, startRevision, endRevision);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public ChangeHistory getAllChanges(File resourceLocation) throws Exception {
        try {
            return changeService.getAllChanges(resourceLocation);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public ChangeHistory getLatestChanges(File resourceLocation, OntologyDocumentRevision startRevision)
            throws Exception {
        try {
            return changeService.getLatestChanges(resourceLocation, startRevision);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public OntologyDocumentRevision getHeadRevision(File resourceLocation) throws Exception {
        try {
            return changeService.getHeadRevision(resourceLocation);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
