package org.protege.owl.server.api;

import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.api.server.ServerPath;

import java.io.File;
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

    protected File getHistoryFile(ProjectId projectId, ServerPath serverPath) throws ServerRequestException {
        try {
            Project project = getConfiguration().getMetaproject().getProjectRegistry().getProject(projectId);
            String rootPath = project.getAddress().get();
            File f = new File(rootPath, serverPath.pathAsString());
            if (!f.exists()) {
                throw new FileNotFoundException();
            }
            return f;
        }
        catch (UnknownProjectIdException e) {
            throw new ServerRequestException(e);
        }
        catch (FileNotFoundException e) {
            throw new ServerRequestException(e);
        }
    }
}
