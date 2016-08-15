package org.protege.editor.owl.server.conflict;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.exception.UnknownProjectIdException;
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

import java.io.IOException;

/**
 * Represents the conflict detection layer that will check if user changes .
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ConflictDetectionFilter extends ServerFilterAdapter {

    private Logger logger = LoggerFactory.getLogger(ConflictDetectionFilter.class);

    public ConflictDetectionFilter(ServerLayer delegate) {
        super(delegate);
    }

    @Override
    public synchronized ChangeHistory commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException {
        try {
            Project project = getConfiguration().getProject(projectId);
            // TODO: head revision is checked here, but another thread may already be proceeding to do a commit
            String projectFilePath = project.getFile().getAbsolutePath();
            HistoryFile historyFile = HistoryFile.openExisting(projectFilePath);
            DocumentRevision serverHeadRevision = getHeadRevision(historyFile);
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
        catch (InvalidHistoryFileException | IOException e) {
            String message = "Unable to access history file in remote server";
            logger.error(printLog(token.getUser(), "Commit changes", message), e);
            throw new ServerServiceException(message, e);
        }
    }

    private DocumentRevision getHeadRevision(HistoryFile historyFile) throws IOException {
        return getChangePool().lookupHead(historyFile);
    }

    private boolean isOutdated(DocumentRevision clientHeadRevision, DocumentRevision serverHeadRevision) {
        return clientHeadRevision.compareTo(serverHeadRevision) < 0;
    }
}
