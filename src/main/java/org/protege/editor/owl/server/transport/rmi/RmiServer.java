package org.protege.editor.owl.server.transport.rmi;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.Server;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.ServerDocument;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Description;
import edu.stanford.protege.metaproject.api.Host;
import edu.stanford.protege.metaproject.api.Name;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationId;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectOptions;
import edu.stanford.protege.metaproject.api.Role;
import edu.stanford.protege.metaproject.api.RoleId;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class RmiServer implements RemoteServer {

    public static final String SERVER_SERVICE = "ProtegeServer";

    private Server server;

    public RmiServer(Server server) {
        this.server = server;
    }

    @Override
    public void createUser(AuthToken token, User newUser)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.createUser(token, newUser);
    }

    @Override
    public void deleteUser(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.deleteUser(token, userId);
    }

    @Override
    public void updateUser(AuthToken token, UserId userId, User user)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.updateUser(token, userId, user);
    }

    @Override
    public ServerDocument createProject(AuthToken token, ProjectId projectId, Name projectName,
            Description description, UserId owner, Optional<ProjectOptions> options)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.createProject(token, projectId, projectName, description, owner, options);
    }

    @Override
    public void deleteProject(AuthToken token, ProjectId projectId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.deleteProject(token, projectId);
    }

    @Override
    public void updateProject(AuthToken token, ProjectId projectId, Project newProject)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.updateProject(token, projectId, newProject);
    }

    @Override
    public ServerDocument openProject(AuthToken token, ProjectId projectId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.openProject(token, projectId);
    }

    @Override
    public void createRole(AuthToken token, Role newRole)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.createRole(token, newRole);
    }

    @Override
    public void deleteRole(AuthToken token, RoleId roleId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.deleteRole(token, roleId);
    }

    @Override
    public void updateRole(AuthToken token, RoleId roleId, Role newRole)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.updateRole(token, roleId, newRole);
    }

    @Override
    public void createOperation(AuthToken token, Operation operation)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.createOperation(token, operation);
    }

    @Override
    public void deleteOperation(AuthToken token, OperationId operationId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.deleteOperation(token, operationId);
    }

    @Override
    public void updateOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.updateOperation(token, operationId, newOperation);
    }

    @Override
    public void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.assignRole(token, userId, projectId, roleId);
    }

    @Override
    public void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.retractRole(token, userId, projectId, roleId);
    }

    @Override
    public Host getHost(AuthToken token) throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getHost(token);
    }

    @Override
    public void setHostAddress(AuthToken token, URI hostAddress)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.setHostAddress(token, hostAddress);
    }

    @Override
    public void setSecondaryPort(AuthToken token, int portNumber)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.setSecondaryPort(token, portNumber);
    }

    @Override
    public String getRootDirectory(AuthToken token)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getRootDirectory(token);
    }

    @Override
    public void setRootDirectory(AuthToken token, String rootDirectory)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.setRootDirectory(token, rootDirectory);
    }

    @Override
    public Map<String, String> getServerProperties(AuthToken token)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getServerProperties(token);
    }

    @Override
    public void setServerProperty(AuthToken token, String property, String value)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.setServerProperty(token, property, value);
    }

    @Override
    public void unsetServerProperty(AuthToken token, String property)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.unsetServerProperty(token, property);
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle changes)
            throws AuthorizationException, ServerServiceException, RemoteException {
        server.commit(token, project, changes);
    }

    @Override
    public List<User> getAllUsers(AuthToken token)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getAllUsers(token);
    }

    @Override
    public List<Project> getProjects(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getProjects(token, userId);
    }

    @Override
    public List<Project> getAllProjects(AuthToken token)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getAllProjects(token);
    }

    @Override
    public Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getRoles(token, userId);
    }

    @Override
    public List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getRoles(token, userId, projectId);
    }

    @Override
    public List<Role> getAllRoles(AuthToken token)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getAllRoles(token);
    }

    @Override
    public Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getOperations(token, userId);
    }

    @Override
    public List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getOperations(token, userId, projectId);
    }

    @Override
    public List<Operation> getAllOperations(AuthToken token)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.getAllOperations(token);
    }

    @Override
    public boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException {
        return server.isOperationAllowed(token, operationId, projectId, userId);
    }
}
