package org.protege.editor.owl.server.conflict;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.exception.UnknownProjectIdException;
import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.ServerFilterAdapter;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.InvalidHistoryFileException;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the conflict detection layer that will check if user changes .
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ConflictDetectionFilter extends ServerFilterAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ConflictDetectionFilter.class);

    private final ChangeService changeService;

    public ConflictDetectionFilter(ServerLayer delegate, ChangeService changeService) {
        super(delegate);
        this.changeService = changeService;
    }

    @Override
    public synchronized ChangeHistory commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException {
        try {
            Project project = getConfiguration().getProject(projectId);
            // TODO: head revision is checked here, but another thread may already be proceeding to do a commit
            String projectFilePath = project.getFile().getAbsolutePath();
            HistoryFile historyFile = HistoryFile.openExisting(projectFilePath);
            DocumentRevision serverHeadRevision = changeService.getHeadRevision(historyFile);
            DocumentRevision commitBaseRevision = commitBundle.getBaseRevision();
            if (isOutdated(commitBaseRevision, serverHeadRevision)) {
                logger.error("Out of sync");
                throw new OutOfSyncException("The local copy is outdated. Please do update.");
            }
            return super.commit(token, projectId, commitBundle);
        }
        catch (UnknownProjectIdException e) {
            logger.error(printLog(token.getUser(), "Commit changes", e.getMessage()));
            throw new ServerServiceException(e.getMessage(), e);
        }
        catch (InvalidHistoryFileException e) {
            String message = "Unable to access history file in remote server";
            logger.error(printLog(token.getUser(), "Commit changes", message), e);
            throw new ServerServiceException(message, e);
        }
    }

    private boolean isOutdated(DocumentRevision clientHeadRevision, DocumentRevision serverHeadRevision) {
        return clientHeadRevision.compareTo(serverHeadRevision) < 0;
    }
}
