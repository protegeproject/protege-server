package org.protege.editor.owl.server.transport.rmi;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Description;
import edu.stanford.protege.metaproject.api.Host;
import edu.stanford.protege.metaproject.api.Name;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationId;
import edu.stanford.protege.metaproject.api.Password;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectOptions;
import edu.stanford.protege.metaproject.api.Role;
import edu.stanford.protege.metaproject.api.RoleId;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.ServerDocument;

import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public interface RemoteServer extends Remote {

    /**
     * Getting all users known by the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return A list of {@code User}.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    List<User> getAllUsers(AuthToken token) throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Creating a new user to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newUser
     *            The new user to add.
     * @param password
     *            The password associated to the new user. Use <code>null</code>
     *            if credential is not required.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void createUser(AuthToken token, User newUser, Password password)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Deleting an existing user from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The user to remove identified by the ID
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void deleteUser(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Updating information of an exiting user in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The target user to modify identified by the ID
     * @param newUser
     *            The new updated user to replace with
     * @param updatedPassword
     *            The new updated password. Use <code>null</code>
     *            if the password is not updated.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void updateUser(AuthToken token, UserId userId, User updatedUser, Password updatedPassword)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Getting all projects the given the user id.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The target user identified by the ID
     * @return A list of {@code Project}
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    List<Project> getProjects(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Getting all project known by the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return A list of {@code Project}.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    List<Project> getAllProjects(AuthToken token)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Creating a new project to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project identifier object
     * @param projectName
     *            The name of the project
     * @param description
     *            The description of the project
     * @param owner
     *            The owner of the project
     * @param options
     *            An optional of project options. Use <code>null</code> if
     *            project options are not required.
     * @return A server document that provide the link information to remote
     *         resources
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    ServerDocument createProject(AuthToken token, ProjectId projectId, Name projectName,
            Description description, UserId owner, ProjectOptions options)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Deleting an existing project from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project to remove identified by its ID.
     * @param includeFile
     *            Remove the associated files.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void deleteProject(AuthToken token, ProjectId projectId, boolean includeFile)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Updating information of an existing project in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The target project to modify identified by its ID.
     * @param updatedProject
     *            The new updated project to replace with.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void updateProject(AuthToken token, ProjectId projectId, Project updatedProject)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Opening a project from the server. The server will return the
     * {@code ProjectResource} that can be used to construct the project
     * ontology.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project to open identified by its ID
     * @return A server document that provide the link information to remote resources
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    ServerDocument openProject(AuthToken token, ProjectId projectId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Getting all roles given the user id, categorized for each owned project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The target user identified by the ID
     * @return A map of {@code ProjectId} with a list of corresponding
     *         {@code Role}
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Getting all roles given the user id and the project id.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The target user identified by the ID
     * @param projectId
     *            The target project identified by the ID
     * @return A list of {@code Role}
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Getting all roles known by the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return A list of {@code Role}
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    List<Role> getAllRoles(AuthToken token) throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Creating a new role to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newRole
     *            The new role to add.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void createRole(AuthToken token, Role newRole)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Deleting an existing role from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param roleId
     *            The role to remove identified by its ID.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void deleteRole(AuthToken token, RoleId roleId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Updating information of an existing role at the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param roleId
     *            The target role to modify identified by its ID.
     * @param updatedRole
     *            The new updated role to replace with.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void updateRole(AuthToken token, RoleId roleId, Role updatedRole)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Getting all operations given the user id, categorized for each owned
     * project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The target user identified by the ID
     * @return A map of {@code ProjectId} with a list of corresponding
     *         {@code Operation}
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Getting all operations given the user id and the project id.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The target user identified by the ID
     * @param projectId
     *            The target project identified by the ID
     * @return A list of {code Operation}
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Getting all operations given the role id
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param roleId
     *            The target role identified by the ID
     * @return A list of {@code Operation}
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    List<Operation> getOperations(AuthToken token, RoleId roleId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Getting all operations known by the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return A list of {@code Operation}
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    List<Operation> getAllOperations(AuthToken token)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Creating a new operation to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operation
     *            The new operation to add.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void createOperation(AuthToken token, Operation operation)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Deleting an existing operation from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *            The operation to remove identified by its ID.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void deleteOperation(AuthToken token, OperationId operationId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Updating information of an existing operation at the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *            The target operation to modify identified by its ID.
     * @param updatedOperation
     *            The new updated operation to replace with.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void updateOperation(AuthToken token, OperationId operationId, Operation updatedOperation)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Assigning a role to a user for a particular project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The target user
     * @param projectId
     *            The target project
     * @param roleId
     *            The role to assign
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Retracting a role from a user for a particular project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The target user
     * @param projectId
     *            The target project
     * @param roleId
     *            The role to retract
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Gets the host information (including the host address and secondary port,
     * if any)
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return The {@code Host} object to represent such information
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    Host getHost(AuthToken token) throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Sets the host server address.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param hostAddress
     *            The host address URI.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void setHostAddress(AuthToken token, URI hostAddress)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Sets the secondary port number.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param portNumber
     *            The port number.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void setSecondaryPort(AuthToken token, int portNumber)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Gets the root directory location.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return The root directory location string.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    String getRootDirectory(AuthToken token) throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Sets the root directory location.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param rootDirectory
     *            The root directory location using the absolute path.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void setRootDirectory(AuthToken token, String rootDirectory)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Gets the map of user's server properties.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return The server property map.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    Map<String, String> getServerProperties(AuthToken token)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Setting a server property by specifying the property name and the value.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param property
     *            The target property name
     * @param value
     *            The property value
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void setServerProperty(AuthToken token, String property, String value)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Unsets a server property by specifying the property name.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param property
     *            The target property name
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    void unsetServerProperty(AuthToken token, String property)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Committing the given ontology changes to be applied in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The target project for such changes
     * @param commitBundle
     *            A list of changes coming from the client
     * @return Returns the change history that contains the changes that are
     *         accepted by the server.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws OutOfSyncException
     *             If the incoming changes are coming from a client's copy that
     *             is out-of-date.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    ChangeHistory commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException, RemoteException;

    /**
     * Checking if an operation is allowed for the given user id and the project
     * id. The operation is usually a project-related action.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *            The target operation
     * @param projectId
     *            The target project
     * @param userId
     *            The target user
     * @return Returns <code>true</code> if a user has the permission to perform
     *         the operation for the specified project, or <code>false</code>
     *         otherwise.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     * @throws RemoteException
     *             If the remote method invocation fails for some reason, e.g.,
     *             communication problems, failure during parameter or return
     *             value marshalling or unmarshalling, protocol errors.
     */
    boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException;

    /**
     * Checking if an operation is allowed for the given user id. The operation
     * is usually an admin-related action.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *            The target operation
     * @param userId
     *            The target user
     * @return Returns <code>true</code> if a user has the permission to perform
     *         the operation, or <code>false</code> otherwise.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     */
    boolean isOperationAllowed(AuthToken token, OperationId operationId, UserId userId)
            throws AuthorizationException, ServerServiceException, RemoteException;
}
