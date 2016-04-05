package org.protege.owl.server.connect;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.exception.ServerRequestException;

import java.rmi.Remote;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.ClientConfiguration;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationId;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.Role;
import edu.stanford.protege.metaproject.api.RoleId;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class RmiServer implements RemoteServer, Remote {

    public static final String SERVER_SERVICE = "ProtegeServer";

    private Server server;

    public RmiServer(Server server) {
        this.server = server;
    }

    @Override
    public ClientConfiguration getClientConfiguration(UserId userId) throws ServerRequestException {
        try {
            return server.getClientConfiguration(userId);
        }
        catch (OWLServerException e) {
            throw new ServerRequestException(e);
        }
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
    public void modifyUser(AuthToken token, UserId userId, User user) throws ServerRequestException {
        server.modifyUser(token, userId, user);
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
    public void modifyProject(AuthToken token, ProjectId projectId, Project newProject) throws ServerRequestException {
        server.modifyProject(token, projectId, newProject);
    }

    @Override
    public void viewProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        server.viewProject(token, projectId);
    }

    @Override
    public void addRole(AuthToken token, Role newRole) throws ServerRequestException {
        server.addRole(token, newRole);
    }

    @Override
    public void removeRole(AuthToken token, RoleId roleId) throws ServerRequestException {
        server.removeRole(token, roleId);
    }

    @Override
    public void modifyRole(AuthToken token, RoleId roleId, Role newRole) throws ServerRequestException {
        server.modifyRole(token, roleId, newRole);
    }

    @Override
    public void addOperation(AuthToken token, Operation operation) throws ServerRequestException {
        server.addOperation(token, operation);
    }

    @Override
    public void removeOperation(AuthToken token, OperationId operationId) throws ServerRequestException {
        server.removeOperation(token, operationId);
    }

    @Override
    public void modifyOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws ServerRequestException {
        server.modifyOperation(token, operationId, newOperation);
    }

    @Override
    public void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws ServerRequestException {
        server.assignRole(token, userId, projectId, roleId);
    }

    @Override
    public void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws ServerRequestException {
        server.retractRole(token, userId, projectId, roleId);
    }

    @Override
    public void modifyServerConfiguration(AuthToken token, String property, String value)
            throws ServerRequestException {
        server.modifyServerConfiguration(token, property, value);
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle changes) throws ServerRequestException {
        server.commit(token, project, changes);
    }
}
