package org.protege.owl.server.api.server;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.changes.ServerDocument;

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
 * Represents all operations that the server can serve to the clients. All
 * operations require an authentication token that will be initially checked
 * before continuing to the execution of the operation.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public interface ServerRequests {

    /**
     * Getting all users known by the server.
     *
     * @param token
     *          An authentication token to verify the request source.
     * @return A list of {@code User}.
     * @throws ServerRequestException
     */
    List<User> getAllUsers(AuthToken token) throws ServerRequestException;

    /**
     * Creating a new user to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newUser
     *            The new user to add.
     * @throws ServerRequestException
     */
    void createUser(AuthToken token, User newUser) throws ServerRequestException;

    /**
     * Deleting an existing user from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The user to remove identified by the ID
     * @throws ServerRequestException
     */
    void deleteUser(AuthToken token, UserId userId) throws ServerRequestException;

    /**
     * Updating information of an exiting user in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user to modify identified by the ID
     * @param newUser
     *          The new updated user to replace with
     * @throws ServerRequestException
     */
    void updateUser(AuthToken token, UserId userId, User updatedUser) throws ServerRequestException;

    /**
     * Getting all projects the given the user id.
     *
     * @param token
     *          An authentication token to verify the request source.
     * @param userId
     *          The target user identified by the ID
     * @return A list of {@code Project}
     * @throws ServerRequestException
     */
    List<Project> getProjects(AuthToken token, UserId userId) throws ServerRequestException;

    /**
     * Getting all project known by the server.
     *
     * @param token
     *          An authentication token to verify the request source.
     * @return A list of {@code Project}.
     * @throws ServerRequestException
     */
    List<Project> getAllProjects(AuthToken token) throws ServerRequestException;

    /**
     * Creating a new project to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newProject
     *            The new project to add.
     * @throws ServerRequestException
     */
    void createProject(AuthToken token, Project newProject) throws ServerRequestException;

    /**
     * Deleting an existing project from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project to remove identified by its ID.
     * @throws ServerRequestException
     */
    void deleteProject(AuthToken token, ProjectId projectId) throws ServerRequestException;

    /**
     * Updating information of an existing project in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The target project to modify identified by its ID.
     * @param updatedProject
     *            The new updated project to replace with.
     * @throws ServerRequestException
     */
    void updateProject(AuthToken token, ProjectId projectId, Project updatedProject) throws ServerRequestException;

    /**
     * Opening a project from the server. The server will return the {@code ProjectResource} that
     * can be used to construct the project ontology.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project to open identified by its ID
     * @throws ServerRequestException
     */
    ServerDocument openProject(AuthToken token, ProjectId projectId) throws ServerRequestException;

    /**
     * Getting all roles given the user id, categorized for each owned project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user identified by the ID
     * @return A map of {@code ProjectId} with a list of corresponding {@code Role}
     * @throws ServerRequestException
     */
    Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId) throws ServerRequestException;

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
     * @throws ServerRequestException
     */
    List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId) throws ServerRequestException;

    /**
     * Getting all roles known by the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return A list of {@code Role}
     * @throws ServerRequestException
     */
    List<Role> getAllRoles(AuthToken token) throws ServerRequestException;

    /**
     * Creating a new role to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newRole
     *          The new role to add.
     * @throws ServerRequestException
     */
    void createRole(AuthToken token, Role newRole) throws ServerRequestException;

    /**
     * Deleting an existing role from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param roleId
     *          The role to remove identified by its ID.
     * @throws ServerRequestException
     */
    void deleteRole(AuthToken token, RoleId roleId) throws ServerRequestException;

    /**
     * Updating information of an existing role at the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param roleId
     *          The target role to modify identified by its ID.
     * @param updatedRole
     *          The new updated role to replace with.
     * @throws ServerRequestException
     */
    void updateRole(AuthToken token, RoleId roleId, Role updatedRole) throws ServerRequestException;

    /**
     * Getting all operations given the user id, categorized for each owned project.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user identified by the ID
     * @return A map of {@code ProjectId} with a list of corresponding {@code Operation}
     * @throws ServerRequestException
     */
    Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId) throws ServerRequestException;

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
     * @throws ServerRequestException
     */
    List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId) throws ServerRequestException;

    /**
     * Getting all operations known by the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @return A list of {@code Operation}
     * @throws ServerRequestException
     */
    List<Operation> getAllOperations(AuthToken token) throws ServerRequestException;

    /**
     * Creating a new operation to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operation
     *          The new operation to add.
     * @throws ServerRequestException
     */
    void createOperation(AuthToken token, Operation operation) throws ServerRequestException;

    /**
     * Deleting an existing operation from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *          The operation to remove identified by its ID.
     * @throws ServerRequestException
     */
    void deleteOperation(AuthToken token, OperationId operationId) throws ServerRequestException;

    /**
     * Updating information of an existing operation at the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *          The target operation to modify identified by its ID.
     * @param updatedOperation
     *          The new updated operation to replace with.
     * @throws ServerRequestException
     */
    void updateOperation(AuthToken token, OperationId operationId, Operation updatedOperation) throws ServerRequestException;

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
     * @throws ServerRequestException
     */
    void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId) throws ServerRequestException;

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
     * @throws ServerRequestException
     */
    void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId) throws ServerRequestException;

    /**
     * Setting a server property by specifying the property name and the value.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param property
     *          The target property name
     * @param value
     *          The property value
     * @throws ServerRequestException
     */
    void setServerConfiguration(AuthToken token, String property, String value) throws ServerRequestException;

    /**
     * Committing the given ontology changes to be applied in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param project
     *            The target project for such changes
     * @param changes
     *            A list of changes coming from the client
     * @throws ServerRequestException
     */
    void commit(AuthToken token, Project project, CommitBundle changes) throws ServerRequestException;

    /**
     * Checking if an operation is allowed for the given user id and the project id.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     * @param projectId
     * @param operationId
     * @return
     * @throws ServerRequestException
     */
    boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId) throws ServerRequestException;
}
