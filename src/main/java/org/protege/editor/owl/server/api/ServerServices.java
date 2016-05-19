package org.protege.editor.owl.server.api;

import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.ServerDocument;

import java.net.URI;
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
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * Represents all operations that the server can serve to the clients. All
 * operations require an authentication token that will be initially checked
 * before continuing to the execution of the operation.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public interface ServerServices {

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
     */
    List<User> getAllUsers(AuthToken token) throws AuthorizationException, ServerServiceException;

    /**
     * Creating a new user to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newUser
     *            The new user to add.
     * @param password
     *            The password associated to the new user.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     */
    void createUser(AuthToken token, User newUser, Optional<SaltedPasswordDigest> password)
            throws AuthorizationException, ServerServiceException;

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
     */
    void deleteUser(AuthToken token, UserId userId) throws AuthorizationException, ServerServiceException;

    /**
     * Updating information of an exiting user in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The target user to modify identified by the ID
     * @param newUser
     *            The new updated user to replace with
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     */
    void updateUser(AuthToken token, UserId userId, User updatedUser)
            throws AuthorizationException, ServerServiceException;

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
     */
    List<Project> getProjects(AuthToken token, UserId userId) throws AuthorizationException, ServerServiceException;

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
     */
    List<Project> getAllProjects(AuthToken token) throws AuthorizationException, ServerServiceException;

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
     *            An optional of project options
     * @return A server document that provide the link information to remote resources
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     */
    ServerDocument createProject(AuthToken token, ProjectId projectId, Name projectName, Description description,
            UserId owner, Optional<ProjectOptions> options) throws AuthorizationException, ServerServiceException;

    /**
     * Deleting an existing project from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project to remove identified by its ID.
     * @throws AuthorizationException
     *             If the user doesn't have the permission to request this
     *             service.
     * @throws ServerServiceException
     *             If the server failed to fulfill the user request.
     */
    void deleteProject(AuthToken token, ProjectId projectId) throws AuthorizationException, ServerServiceException;

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
     */
    void updateProject(AuthToken token, ProjectId projectId, Project updatedProject)
            throws AuthorizationException, ServerServiceException;

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
     */
    ServerDocument openProject(AuthToken token, ProjectId projectId)
            throws AuthorizationException, ServerServiceException;

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
     */
    Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException;

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
     */
    List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId)
            throws AuthorizationException, ServerServiceException;

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
     */
    List<Role> getAllRoles(AuthToken token) throws AuthorizationException, ServerServiceException;

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
     */
    void createRole(AuthToken token, Role newRole) throws AuthorizationException, ServerServiceException;

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
     */
    void deleteRole(AuthToken token, RoleId roleId) throws AuthorizationException, ServerServiceException;

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
     */
    void updateRole(AuthToken token, RoleId roleId, Role updatedRole)
            throws AuthorizationException, ServerServiceException;

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
     */
    Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId)
            throws AuthorizationException, ServerServiceException;

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
     */
    List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId)
            throws AuthorizationException, ServerServiceException;

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
     */
    List<Operation> getAllOperations(AuthToken token) throws AuthorizationException, ServerServiceException;

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
     */
    void createOperation(AuthToken token, Operation operation) throws AuthorizationException, ServerServiceException;

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
     */
    void deleteOperation(AuthToken token, OperationId operationId)
            throws AuthorizationException, ServerServiceException;

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
     */
    void updateOperation(AuthToken token, OperationId operationId, Operation updatedOperation)
            throws AuthorizationException, ServerServiceException;

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
     */
    void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException;

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
     */
    void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException;

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
     */
    Host getHost(AuthToken token) throws AuthorizationException, ServerServiceException;

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
     */
    void setHostAddress(AuthToken token, URI hostAddress) throws AuthorizationException, ServerServiceException;

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
     */
    void setSecondaryPort(AuthToken token, int portNumber) throws AuthorizationException, ServerServiceException;

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
     */
    String getRootDirectory(AuthToken token) throws AuthorizationException, ServerServiceException;

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
     */
    void setRootDirectory(AuthToken token, String rootDirectory) throws AuthorizationException, ServerServiceException;

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
     */
    Map<String, String> getServerProperties(AuthToken token) throws AuthorizationException, ServerServiceException;

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
     */
    void setServerProperty(AuthToken token, String property, String value)
            throws AuthorizationException, ServerServiceException;

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
     */
    void unsetServerProperty(AuthToken token, String property) throws AuthorizationException, ServerServiceException;

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
     */
    ChangeHistory commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException;

    /**
     * Checking if an operation is allowed for the given user id and the project
     * id.
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
     */
    boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId)
            throws AuthorizationException, ServerServiceException;
}
