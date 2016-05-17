package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.ServerFilterAdapter;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.TransportHandler;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OWLServerException;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.protege.editor.owl.server.versioning.api.ServerDocument;

import java.util.Optional;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Description;
import edu.stanford.protege.metaproject.api.Name;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectOptions;
import edu.stanford.protege.metaproject.api.ProjectRegistry;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * Represents the change document layer that will validate the user changes in the commit document.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ConflictDetectionFilter extends ServerFilterAdapter {

    private ChangeDocumentPool changePool = new ChangeDocumentPool();

    private ChangeService changeService;

    public ConflictDetectionFilter(ServerLayer delegate) {
        super(delegate);
        changeService = new DefaultChangeService(changePool);
    }

    public void setLoginService(ChangeService changeService) {
        this.changeService = changeService;
    }

    @Override
    public ServerDocument createProject(AuthToken token, ProjectId projectId, Name projectName,
            Description description, UserId owner, Optional<ProjectOptions> options)
            throws AuthorizationException, ServerServiceException {
        ServerDocument serverDocument = super.createProject(token, projectId, projectName, description, owner, options);
        changePool.put(serverDocument.getHistoryFile(), ChangeHistoryImpl.createEmptyChangeHistory());
        return serverDocument;
    }
    
    @Override
    public void commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException {
        try {
            Project project = getConfiguration().getMetaproject().getProjectRegistry().get(projectId);
            HistoryFile historyFile = HistoryFile.openExisting(project.getFile().getAbsolutePath());
            DocumentRevision serverHeadRevision = changeService.getHeadRevision(historyFile);
            DocumentRevision clientHeadRevision = commitBundle.getHeadRevision();
            if (isOutdated(clientHeadRevision, serverHeadRevision)) {
                throw new OutOfSyncException("The server contains changes that you do not have locally");
            }
        }
        catch (InvalidHistoryFileException e) {
            throw new ServerServiceException("Could not found remote history file", e);
        }
        catch (Exception e) {
            throw new ServerServiceException("Could not retreive remote head revision", e);
        }
        super.commit(token, projectId, commitBundle);
    }

    private boolean isOutdated(DocumentRevision clientHeadRevision, DocumentRevision serverHeadRevision) {
        return clientHeadRevision.compareTo(serverHeadRevision) < 0;
    }

    @Override
    public void setTransport(TransportHandler transport) throws OWLServerException {
        try {
            transport.bind(changeService);
        }
        catch (Exception e) {
            throw new OWLServerException(e);
        }
        super.setTransport(transport);
    }
}
