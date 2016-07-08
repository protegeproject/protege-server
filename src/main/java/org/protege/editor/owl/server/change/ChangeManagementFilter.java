package org.protege.editor.owl.server.change;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Description;
import edu.stanford.protege.metaproject.api.Name;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectOptions;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.UnknownMetaprojectObjectIdException;

import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.ServerFilterAdapter;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OWLServerException;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.ChangeHistoryImpl;
import org.protege.editor.owl.server.versioning.InvalidHistoryFileException;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.protege.editor.owl.server.versioning.api.ServerDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Represents the change history manager that stores new changes from users.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ChangeManagementFilter extends ServerFilterAdapter {

    private Logger logger = LoggerFactory.getLogger(ChangeManagementFilter.class);

    private ChangeService changeService;

    public ChangeManagementFilter(ServerLayer delegate) {
        super(delegate);
        changeService = new DefaultChangeService(getChangePool());
    }

    @Override
    public ServerDocument createProject(AuthToken token, ProjectId projectId, Name projectName, Description description,
            UserId owner, Optional<ProjectOptions> options) throws AuthorizationException, ServerServiceException {
        ServerDocument serverDocument = super.createProject(token, projectId, projectName, description, owner, options);
        getChangePool().update(serverDocument.getHistoryFile(), ChangeHistoryImpl.createEmptyChangeHistory());
        return serverDocument;
    }

    @Override
    public ChangeHistory commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException {
        try {
            ChangeHistory changeHistory = super.commit(token, projectId, commitBundle);
            Project project = getConfiguration().getMetaproject().getProjectRegistry().get(projectId);
            HistoryFile historyFile = HistoryFile.openExisting(project.getFile().getPath());
            getChangePool().update(historyFile, changeHistory);
            return changeHistory;
        }
        catch (UnknownMetaprojectObjectIdException e) {
            logger.error(printLog(token.getUser(), "Commit changes", e.getMessage()));
            throw new ServerServiceException(e.getMessage(), e);
        }
        catch (InvalidHistoryFileException e) {
            logger.error(printLog(token.getUser(), "Commit changes", e.getMessage()), e);
            throw new ServerServiceException(e.getMessage(), e);
        }
    }

    
}
