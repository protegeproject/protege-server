package org.protege.owl.server.api;

import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.api.server.ServerPath;
import org.protege.owl.server.changes.HistoryFile;

import java.io.FileNotFoundException;

import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.exception.UnknownProjectIdException;

public abstract class ServerLayer implements Server {

    /**
     * Get the server configuration
     *
     * @return Server configuration
     */
    protected abstract ServerConfiguration getConfiguration();

    protected HistoryFile getHistoryFile(ProjectId projectId, ServerPath serverPath) throws ServerRequestException {
        try {
            Project project = getConfiguration().getMetaproject().getProjectRegistry().getProject(projectId);
            String rootPath = project.getAddress().get();
            HistoryFile historyFile = new HistoryFile(rootPath, serverPath.pathAsString());
            if (!historyFile.exists()) {
                throw new FileNotFoundException(); // TODO: Use factory to create the history file
            }
            return historyFile;
        }
        catch (UnknownProjectIdException e) {
            throw new ServerRequestException(e);
        }
        catch (FileNotFoundException e) {
            throw new ServerRequestException(e);
        }
    }
}
