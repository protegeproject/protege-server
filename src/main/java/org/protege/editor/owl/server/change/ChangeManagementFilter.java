package org.protege.editor.owl.server.change;

import edu.stanford.protege.metaproject.api.*;
import edu.stanford.protege.metaproject.api.exception.UnknownProjectIdException;
import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.ServerFilterAdapter;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
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
        getChangePool().appendChanges(serverDocument.getHistoryFile(), ChangeHistoryImpl.createEmptyChangeHistory());
        return serverDocument;
    }

    @Override
    public ChangeHistory commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException {
        try {
            ChangeHistory changeHistory = super.commit(token, projectId, commitBundle);
            Project project = getConfiguration().getProject(projectId);
            String projectFilePath = project.getFile().getAbsolutePath();
            HistoryFile historyFile = HistoryFile.openExisting(projectFilePath);
            getChangePool().appendChanges(historyFile, changeHistory);
            return changeHistory;
        }
        catch (UnknownProjectIdException e) {
            logger.error(printLog(token.getUser(), "Commit changes", e.getMessage()));
            throw new ServerServiceException(e.getMessage(), e);
        }
        catch (InvalidHistoryFileException e) {
            logger.error(printLog(token.getUser(), "Commit changes", e.getMessage()), e);
            throw new ServerServiceException(e.getMessage(), e);
        }
    }

    
}
