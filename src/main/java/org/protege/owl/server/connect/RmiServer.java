package org.protege.owl.server.connect;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.exception.ServerRequestException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class RmiServer implements RemoteServer {

    public static final String SERVER_SERVICE = "ProtegeServer";

    private Server server;

    public RmiServer(Server server) {
        this.server = server;
    }

    @Override
    public void addUser(AuthToken token, User newUser) throws ServerRequestException {
        server.addUser(token, newUser);
    }

    @Override
    public void removeUser(AuthToken token, UserId userId) throws ServerRequestException {
        server.removeUser(token, userId);
    }

    @Override
    public void addProject(AuthToken token, Project newProject) throws ServerRequestException {
        server.addProject(token, newProject);
    }

    @Override
    public void removeProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        server.removeProject(token, projectId);
    }

    @Override
    public void viewProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        server.viewProject(token, projectId);
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle changes) throws ServerRequestException {
        server.commit(token, project, changes);
    }
}
