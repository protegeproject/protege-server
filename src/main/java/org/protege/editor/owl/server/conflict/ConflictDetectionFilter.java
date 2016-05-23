package org.protege.editor.owl.server.conflict;

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

import java.io.IOException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.exception.UnknownMetaprojectObjectIdException;

/**
 * Represents the conflict detection layer that will check if user changes .
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ConflictDetectionFilter extends ServerFilterAdapter {

    public ConflictDetectionFilter(ServerLayer delegate) {
        super(delegate);
    }

    @Override
    public ChangeHistory commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException {
        try {
            Project project = getConfiguration().getMetaproject().getProjectRegistry().get(projectId);
            HistoryFile historyFile = HistoryFile.openExisting(project.getFile().getAbsolutePath());
            DocumentRevision serverHeadRevision = getHeadRevision(historyFile);
            DocumentRevision clientHeadRevision = commitBundle.getHeadRevision();
            if (isOutdated(clientHeadRevision, serverHeadRevision)) {
                throw new OutOfSyncException("The local copy is outdated. Please do update.");
            }
            return super.commit(token, projectId, commitBundle);
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException("Unknown project ID: " + projectId.get(), e);
        }
        catch (InvalidHistoryFileException | IOException e) {
            throw new ServerServiceException(e.getMessage());
        }
    }

    private DocumentRevision getHeadRevision(HistoryFile historyFile) throws IOException {
        return getChangePool().lookup(historyFile).getHeadRevision();
    }

    private boolean isOutdated(DocumentRevision clientHeadRevision, DocumentRevision serverHeadRevision) {
        return clientHeadRevision.compareTo(serverHeadRevision) < 0;
    }
}
