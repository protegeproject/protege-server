package org.protege.owl.server.connect;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.changes.ServerDocument;

import java.rmi.Remote;
import java.util.List;
import java.util.Map;

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
    public void createUser(AuthToken token, User newUser) throws ServerRequestException {
        server.createUser(token, newUser);
    }

    @Override
    public void deleteUser(AuthToken token, UserId userId) throws ServerRequestException {
        server.deleteUser(token, userId);
    }

    @Override
    public void updateUser(AuthToken token, UserId userId, User user) throws ServerRequestException {
        server.updateUser(token, userId, user);
    }

    @Override
    public void createProject(AuthToken token, Project newProject) throws ServerRequestException {
        server.createProject(token, newProject);
    }

    @Override
    public void deleteProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        server.deleteProject(token, projectId);
    }

    @Override
    public void updateProject(AuthToken token, ProjectId projectId, Project newProject) throws ServerRequestException {
        server.updateProject(token, projectId, newProject);
    }

    @Override
    public ServerDocument openProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        return server.openProject(token, projectId);
    }

    @Override
    public void createRole(AuthToken token, Role newRole) throws ServerRequestException {
        server.createRole(token, newRole);
    }

    @Override
    public void deleteRole(AuthToken token, RoleId roleId) throws ServerRequestException {
        server.deleteRole(token, roleId);
    }

    @Override
    public void updateRole(AuthToken token, RoleId roleId, Role newRole) throws ServerRequestException {
        server.updateRole(token, roleId, newRole);
    }

    @Override
    public void createOperation(AuthToken token, Operation operation) throws ServerRequestException {
        server.createOperation(token, operation);
    }

    @Override
    public void deleteOperation(AuthToken token, OperationId operationId) throws ServerRequestException {
        server.deleteOperation(token, operationId);
    }

    @Override
    public void updateOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws ServerRequestException {
        server.updateOperation(token, operationId, newOperation);
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
    public void setServerConfiguration(AuthToken token, String property, String value)
            throws ServerRequestException {
        server.setServerConfiguration(token, property, value);
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle changes) throws ServerRequestException {
        server.commit(token, project, changes);
    }

    @Override
    public List<User> getAllUsers(AuthToken token) throws ServerRequestException {
        return server.getAllUsers(token);
    }

    @Override
    public List<Project> getProjects(AuthToken token, UserId userId) throws ServerRequestException {
        return server.getProjects(token, userId);
    }

    @Override
    public List<Project> getAllProjects(AuthToken token) throws ServerRequestException {
        return server.getAllProjects(token);
    }

    @Override
    public Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId) throws ServerRequestException {
        return server.getRoles(token, userId);
    }

    @Override
    public List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId) throws ServerRequestException {
        return server.getRoles(token, userId, projectId);
    }

    @Override
    public List<Role> getAllRoles(AuthToken token) throws ServerRequestException {
        return server.getAllRoles(token);
    }

    @Override
    public Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId) throws ServerRequestException {
        return server.getOperations(token, userId);
    }

    @Override
    public List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId)
            throws ServerRequestException {
        return server.getOperations(token, userId, projectId);
    }

    @Override
    public List<Operation> getAllOperations(AuthToken token) throws ServerRequestException {
        return server.getAllOperations(token);
    }

    @Override
    public boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId)
            throws ServerRequestException {
        return server.isOperationAllowed(token, operationId, projectId, userId);
    }
}
