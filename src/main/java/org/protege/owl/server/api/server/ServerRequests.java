package org.protege.owl.server.api.server;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.exception.ServerRequestException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
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
     * Committing the given ontology changes to be applied in the server.
     *
     * @param token
     *            An authentication token to verify the request source.
     * @param projectId
     *            The target project for such changes
     * @param changes
     *            A list of changes coming from the client
     * @throws ServerRequestException
     */
    void commit(AuthToken token, ProjectId projectId, CommitBundle changes) throws ServerRequestException;
}
