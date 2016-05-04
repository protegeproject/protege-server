package org.protege.editor.owl.server.security;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.LoginService;
import org.protege.editor.owl.server.api.ServerFilterAdapter;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.TransportHandler;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OWLServerException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.ServerDocument;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.AuthenticationRegistry;
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
import edu.stanford.protege.metaproject.api.UserRegistry;

/**
 * Represents the authentication gate that will validate the user session in the server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AuthenticationFilter extends ServerFilterAdapter {

    private final AuthenticationRegistry authRegistry;

    private SessionManager sessionManager = new SessionManager();

    private LoginService loginService;

    public AuthenticationFilter(ServerLayer delegate) {
        super(delegate);
        authRegistry = getConfiguration().getAuthenticationRegistry();
        UserRegistry userRegistry = getConfiguration().getMetaproject().getUserRegistry();
        loginService = new DefaultLoginService(authRegistry, userRegistry, sessionManager);
    }

    public void setLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    protected void verifyRequest(AuthToken token) throws AuthorizationException {
        if (!sessionManager.check(token)) {
            throw new AuthorizationException("Access denied");
        }
    }

    @Override
    public void createUser(AuthToken token, User newUser) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.createUser(token, newUser);
    }

    @Override
    public void deleteUser(AuthToken token, UserId userId) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.deleteUser(token, userId);
    }

    @Override
    public void updateUser(AuthToken token, UserId userId, User user)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.updateUser(token, userId, user);
    }

    @Override
    public ServerDocument createProject(AuthToken token, ProjectId projectId, Name projectName,
           Description description, UserId owner, Optional<ProjectOptions> options)
           throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.createProject(token, projectId, projectName, description, owner, options);
    }

    @Override
    public void deleteProject(AuthToken token, ProjectId projectId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.deleteProject(token, projectId);
    }

    @Override
    public void updateProject(AuthToken token, ProjectId projectId, Project newProject)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.updateProject(token, projectId, newProject);
    }

    @Override
    public ServerDocument openProject(AuthToken token, ProjectId projectId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.openProject(token, projectId);
    }

    @Override
    public void createRole(AuthToken token, Role newRole) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.createRole(token, newRole);
    }

    @Override
    public void deleteRole(AuthToken token, RoleId roleId) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.deleteRole(token, roleId);
    }

    @Override
    public void updateRole(AuthToken token, RoleId roleId, Role newRole)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.updateRole(token, roleId, newRole);
    }

    @Override
    public void createOperation(AuthToken token, Operation operation)
            throws AuthorizationException, ServerServiceException {
       verifyRequest(token);
       super.createOperation(token, operation);
    }

    @Override
    public void deleteOperation(AuthToken token, OperationId operationId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.deleteOperation(token, operationId);
    }

    @Override
    public void updateOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.updateOperation(token, operationId, newOperation);
    }

    @Override
    public void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.assignRole(token, userId, projectId, roleId);
    }

    @Override
    public void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.retractRole(token, userId, projectId, roleId);
    }

    @Override
    public Host getHost(AuthToken token) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getHost(token);
    }

    @Override
    public void setHostAddress(AuthToken token, URI hostAddress) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.setHostAddress(token, hostAddress);
    }

    @Override
    public void setSecondaryPort(AuthToken token, int portNumber)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.setSecondaryPort(token, portNumber);
    }

    @Override
    public String getRootDirectory(AuthToken token) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getRootDirectory(token);
    }

    @Override
    public void setRootDirectory(AuthToken token, String rootDirectory)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.setRootDirectory(token, rootDirectory);
    }

    @Override
    public Map<String, String> getServerProperties(AuthToken token)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getServerProperties(token);
    }

    @Override
    public void setServerProperty(AuthToken token, String property, String value)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.setServerProperty(token, property, value);
    }

    @Override
    public void unsetServerProperty(AuthToken token, String property)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.unsetServerProperty(token, property);
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle changes)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        super.commit(token, project, changes);
    }

    @Override
    public List<User> getAllUsers(AuthToken token) throws AuthorizationException, ServerServiceException {
       verifyRequest(token);
      return super.getAllUsers(token);
    }

    @Override
    public List<Project> getProjects(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getProjects(token, userId);
    }

    @Override
    public List<Project> getAllProjects(AuthToken token) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getAllProjects(token);
    }

    @Override
    public Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getRoles(token, userId);
    }

    @Override
    public List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getRoles(token, userId, projectId);
    }

    @Override
    public List<Role> getAllRoles(AuthToken token) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getAllRoles(token);
    }

    @Override
    public Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getOperations(token, userId);
    }

    @Override
    public List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getOperations(token, userId, projectId);
    }

    @Override
    public List<Operation> getAllOperations(AuthToken token) throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.getAllOperations(token);
    }

    @Override
    public boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId)
            throws AuthorizationException, ServerServiceException {
        verifyRequest(token);
        return super.isOperationAllowed(token, operationId, projectId, userId);
    }

    @Override
    public void setTransport(TransportHandler transport) throws OWLServerException {
        try {
            transport.bind(loginService);
        }
        catch (Exception e) {
            throw new OWLServerException(e);
        }
        super.setTransport(transport);
    }
}
