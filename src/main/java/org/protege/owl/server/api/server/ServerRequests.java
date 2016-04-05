package org.protege.owl.server.api.server;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.exception.ServerRequestException;

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
 * Represents all operations that the client could request to the server. All
 * operations require an authentication token that will be initially checked
 * before continuing to the execution of the operation.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public interface ServerRequests {

    /**
     * Adding a new user to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newUser
     *            The new user to add.
     * @throws ServerRequestException
     */
    void addUser(AuthToken token, User newUser) throws ServerRequestException;

    /**
     * Removing an existing user from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *            The user to remove identified by the ID
     * @throws ServerRequestException
     */
    void removeUser(AuthToken token, UserId userId) throws ServerRequestException;

    /**
     * Modifying an exiting user in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param userId
     *          The target user to modify identified by the ID
     * @param newUser
     *          The new user to replace with
     * @throws ServerRequestException
     */
    void modifyUser(AuthToken token, UserId userId, User user) throws ServerRequestException;

    /**
     * Adding a new project to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newProject
     *            The new project to add.
     * @throws ServerRequestException
     */
    void addProject(AuthToken token, Project newProject) throws ServerRequestException;

    /**
     * Removing an existing project from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project to remove identified by its ID.
     * @throws ServerRequestException
     */
    void removeProject(AuthToken token, ProjectId projectId) throws ServerRequestException;

    /**
     * Modifying an existing project in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The target project to modify identified by its ID.
     * @param newProject
     *            The new project to replace with.
     * @throws ServerRequestException
     */
    void modifyProject(AuthToken token, ProjectId projectId, Project newProject) throws ServerRequestException;

    /**
     * Viewing only the project (similar to read-only permission) from the
     * server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The project to view identified by its ID
     * @throws ServerRequestException
     */
    void viewProject(AuthToken token, ProjectId projectId) throws ServerRequestException;

    /**
     * Adding a new role to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param newRole
     *          The new role to add.
     * @throws ServerRequestException
     */
    void addRole(AuthToken token, Role newRole) throws ServerRequestException;

    /**
     * Removing an existing role from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param roleId
     *          The role to remove identified by its ID.
     * @throws ServerRequestException
     */
    void removeRole(AuthToken token, RoleId roleId) throws ServerRequestException;

    /**
     * Modifying an existing role at the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param roleId
     *          The target role to modify identified by its ID.
     * @param newRole
     *          The new role to replace with.
     * @throws ServerRequestException
     */
    void modifyRole(AuthToken token, RoleId roleId, Role newRole) throws ServerRequestException;

    /**
     * Adding a new operation to the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operation
     *          The new operation to add.
     * @throws ServerRequestException
     */
    void addOperation(AuthToken token, Operation operation) throws ServerRequestException;

    /**
     * Removing an existing operation from the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *          The operation to remove identified by its ID.
     * @throws ServerRequestException
     */
    void removeOperation(AuthToken token, OperationId operationId) throws ServerRequestException;

    /**
     * Modifying an existing operation at the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param operationId
     *          The target operation to modify identified by its ID.
     * @param newOperation
     *          The new operation to replace with.
     * @throws ServerRequestException
     */
    void modifyOperation(AuthToken token, OperationId operationId, Operation newOperation) throws ServerRequestException;

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
     * Modifying an existing server property by replacing the value.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param property
     *          The target property name
     * @param value
     *          The new property value
     * @throws ServerRequestException
     */
    void modifyServerConfiguration(AuthToken token, String property, String value) throws ServerRequestException;

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
}
