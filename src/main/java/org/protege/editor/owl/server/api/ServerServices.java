package org.protege.editor.owl.server.api;

import org.protege.editor.owl.server.versioning.ServerDocument;

import java.util.List;
import java.util.Map;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Host;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationId;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.Role;
import edu.stanford.protege.metaproject.api.RoleId;
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
     *          An authentication token to verify the request source.
     * @return A list of {@code User}.
     * @throws Exception
     */
    List<User> getAllUsers(AuthToken token) throws Exception;

    /**
     * Creating a new user to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newUser
     *            The new user to add.
     * @throws Exception
     */
    void createUser(AuthToken token, User newUser) throws Exception;

    /**
     * Deleting an existing user from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The user to remove identified by the ID
     * @throws Exception
     */
    void deleteUser(AuthToken token, UserId userId) throws Exception;

    /**
     * Updating information of an exiting user in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user to modify identified by the ID
     * @param newUser
     *          The new updated user to replace with
     * @throws Exception
     */
    void updateUser(AuthToken token, UserId userId, User updatedUser) throws Exception;

    /**
     * Getting all projects the given the user id.
     *
     * @param token
     *          An authentication token to verify the request source.
     * @param userId
     *          The target user identified by the ID
     * @return A list of {@code Project}
     * @throws Exception
     */
    List<Project> getProjects(AuthToken token, UserId userId) throws Exception;

    /**
     * Getting all project known by the server.
     *
     * @param token
     *          An authentication token to verify the request source.
     * @return A list of {@code Project}.
     * @throws Exception
     */
    List<Project> getAllProjects(AuthToken token) throws Exception;

    /**
     * Creating a new project to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newProject
     *            The new project to add.
     * @throws Exception
     */
    void createProject(AuthToken token, Project newProject) throws Exception;

    /**
     * Deleting an existing project from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project to remove identified by its ID.
     * @throws Exception
     */
    void deleteProject(AuthToken token, ProjectId projectId) throws Exception;

    /**
     * Updating information of an existing project in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The target project to modify identified by its ID.
     * @param updatedProject
     *            The new updated project to replace with.
     * @throws Exception
     */
    void updateProject(AuthToken token, ProjectId projectId, Project updatedProject) throws Exception;

    /**
     * Opening a project from the server. The server will return the {@code ProjectResource} that
     * can be used to construct the project ontology.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project to open identified by its ID
     * @throws Exception
     */
    ServerDocument openProject(AuthToken token, ProjectId projectId) throws Exception;

    /**
     * Getting all roles given the user id, categorized for each owned project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user identified by the ID
     * @return A map of {@code ProjectId} with a list of corresponding {@code Role}
     * @throws Exception
     */
    Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId) throws Exception;

    /**
     * Getting all roles given the user id and the project id.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user identified by the ID
     * @param projectId
     *          The target project identified by the ID
     * @return A list of {@code Role}
     * @throws Exception
     */
    List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId) throws Exception;

    /**
     * Getting all roles known by the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return A list of {@code Role}
     * @throws Exception
     */
    List<Role> getAllRoles(AuthToken token) throws Exception;

    /**
     * Creating a new role to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newRole
     *          The new role to add.
     * @throws Exception
     */
    void createRole(AuthToken token, Role newRole) throws Exception;

    /**
     * Deleting an existing role from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param roleId
     *          The role to remove identified by its ID.
     * @throws Exception
     */
    void deleteRole(AuthToken token, RoleId roleId) throws Exception;

    /**
     * Updating information of an existing role at the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param roleId
     *          The target role to modify identified by its ID.
     * @param updatedRole
     *          The new updated role to replace with.
     * @throws Exception
     */
    void updateRole(AuthToken token, RoleId roleId, Role updatedRole) throws Exception;

    /**
     * Getting all operations given the user id, categorized for each owned project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user identified by the ID
     * @return A map of {@code ProjectId} with a list of corresponding {@code Operation}
     * @throws Exception
     */
    Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId) throws Exception;

    /**
     * Getting all operations given the user id and the project id.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user identified by the ID
     * @param projectId
     *          The target project identified by the ID
     * @return A list of {code Operation}
     * @throws Exception
     */
    List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId) throws Exception;

    /**
     * Getting all operations known by the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return A list of {@code Operation}
     * @throws Exception
     */
    List<Operation> getAllOperations(AuthToken token) throws Exception;

    /**
     * Creating a new operation to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operation
     *          The new operation to add.
     * @throws Exception
     */
    void createOperation(AuthToken token, Operation operation) throws Exception;

    /**
     * Deleting an existing operation from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *          The operation to remove identified by its ID.
     * @throws Exception
     */
    void deleteOperation(AuthToken token, OperationId operationId) throws Exception;

    /**
     * Updating information of an existing operation at the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *          The target operation to modify identified by its ID.
     * @param updatedOperation
     *          The new updated operation to replace with.
     * @throws Exception
     */
    void updateOperation(AuthToken token, OperationId operationId, Operation updatedOperation) throws Exception;

    /**
     * Assigning a role to a user for a particular project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user
     * @param projectId
     *          The target project
     * @param roleId
     *          The role to assign
     * @throws Exception
     */
    void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId) throws Exception;

    /**
     * Retracting a role from a user for a particular project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user
     * @param projectId 
     *          The target project
     * @param roleId
     *          The role to retract
     * @throws Exception
     */
    void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId) throws Exception;

    /**
     * Gets the host information (including the host address and secondary port, if any)
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return The {@code Host} object to represent such information
     * @throws Exception
     */
    Host getHost(AuthToken token) throws Exception;

    /**
     * Sets the host server address.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param hostAddress
     *          The host address string.
     * @throws Exception
     */
    void setHostAddress(AuthToken token, String hostAddress) throws Exception;

    /**
     * Sets the secondary port number.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param portNumber
     *          The port number.
     * @throws Exception
     */
    void setSecondaryPort(AuthToken token, int portNumber) throws Exception;

    /**
     * Gets the root directory location.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return The root directory location string.
     * @throws Exception
     */
    String getRootDirectory(AuthToken token) throws Exception;

    /**
     * Sets the root directory location.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param rootDirectory
     *          The root directory location using the absolute path.
     * @throws Exception
     */
    void setRootDirectory(AuthToken token, String rootDirectory) throws Exception;

    /**
     * Gets the map of user's server properties.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return The server property map.
     * @throws Exception
     */
    Map<String, String> getServerProperties(AuthToken token) throws Exception;

    /**
     * Setting a server property by specifying the property name and the value.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param property
     *          The target property name
     * @param value
     *          The property value
     * @throws Exception
     */
    void setServerProperty(AuthToken token, String property, String value) throws Exception;

    /**
     * Unsets a server property by specifying the property name.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param property
     *          The target property name
     * @throws Exception
     */
    void unsetServerProperty(AuthToken token, String property) throws Exception;

    /**
     * Committing the given ontology changes to be applied in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param project
     *            The target project for such changes
     * @param changes
     *            A list of changes coming from the client
     * @throws Exception
     */
    void commit(AuthToken token, Project project, CommitBundle changes) throws Exception;

    /**
     * Checking if an operation is allowed for the given user id and the project id.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     * @param projectId
     * @param operationId
     * @return
     * @throws Exception
     */
    boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId) throws Exception;
}
