package org.protege.owl.server.connect;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.changes.ServerDocument;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import edu.stanford.protege.metaproject.api.AuthToken;
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
public class RmiServer implements RemoteServer {

    public static final String SERVER_SERVICE = "ProtegeServer";

    private Server server;

    public RmiServer(Server server) {
        this.server = server;
    }

    @Override
    public void createUser(AuthToken token, User newUser) throws RemoteException {
        try {
            server.createUser(token, newUser);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(AuthToken token, UserId userId) throws RemoteException {
        try {
            server.deleteUser(token, userId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void updateUser(AuthToken token, UserId userId, User user) throws RemoteException {
        try {
            server.updateUser(token, userId, user);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void createProject(AuthToken token, Project newProject) throws RemoteException {
        try {
            server.createProject(token, newProject);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteProject(AuthToken token, ProjectId projectId) throws RemoteException {
        try {
            server.deleteProject(token, projectId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void updateProject(AuthToken token, ProjectId projectId, Project newProject) throws RemoteException {
        try {
            server.updateProject(token, projectId, newProject);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public ServerDocument openProject(AuthToken token, ProjectId projectId) throws RemoteException {
        try {
            return server.openProject(token, projectId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void createRole(AuthToken token, Role newRole) throws RemoteException {
        try {
            server.createRole(token, newRole);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteRole(AuthToken token, RoleId roleId) throws RemoteException {
        try {
            server.deleteRole(token, roleId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void updateRole(AuthToken token, RoleId roleId, Role newRole) throws RemoteException {
        try {
            server.updateRole(token, roleId, newRole);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void createOperation(AuthToken token, Operation operation) throws RemoteException {
        try {
            server.createOperation(token, operation);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteOperation(AuthToken token, OperationId operationId) throws RemoteException {
        try {
            server.deleteOperation(token, operationId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void updateOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws RemoteException {
        try {
            server.updateOperation(token, operationId, newOperation);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId) throws RemoteException {
        try {
            server.assignRole(token, userId, projectId, roleId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId) throws RemoteException {
        try {
            server.retractRole(token, userId, projectId, roleId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void setServerConfiguration(AuthToken token, String property, String value) throws RemoteException {
        try {
            server.setServerConfiguration(token, property, value);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle changes) throws RemoteException {
        try {
            server.commit(token, project, changes);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAllUsers(AuthToken token) throws RemoteException {
        try {
            return server.getAllUsers(token);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<Project> getProjects(AuthToken token, UserId userId) throws RemoteException {
        try {
            return server.getProjects(token, userId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<Project> getAllProjects(AuthToken token) throws RemoteException {
        try {
            return server.getAllProjects(token);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId) throws RemoteException {
        try {
            return server.getRoles(token, userId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId) throws RemoteException {
        try {
            return server.getRoles(token, userId, projectId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<Role> getAllRoles(AuthToken token) throws RemoteException {
        try {
            return server.getAllRoles(token);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId) throws RemoteException {
        try {
            return server.getOperations(token, userId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId) throws RemoteException {
        try {
            return server.getOperations(token, userId, projectId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<Operation> getAllOperations(AuthToken token) throws RemoteException {
        try {
            return server.getAllOperations(token);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId)
            throws RemoteException {
        try {
            return server.isOperationAllowed(token, operationId, projectId, userId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
