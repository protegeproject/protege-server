package org.protege.owl.server.connect;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.api.server.Server;

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

    private Server server;

    public RmiServer(Server server) {
        this.server = server;
    }

    @Override
    public void addUser(AuthToken token, User newUser) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeUser(AuthToken token, UserId userId) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addProject(AuthToken token, Project newProject) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void viewProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void commit(AuthToken token, ProjectId projectId, CommitBundle changes) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void start(AuthToken token) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop(AuthToken token) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }
}
