package org.protege.owl.server.api_new;

import org.protege.owl.server.api.User;

import java.util.Collection;

/**
 * A manager for the association of users and roles
 *
 * @author Rafael Gonçalves
 * Stanford Center for Biomedical Informatics Research
 */
public interface UserRoleManager {

    /**
     * Add a user to the user-role registry with default role
     *
     * @param user  User to be added
     * @return true if user was successfully added to the registry, false otherwise
     */
    boolean addUser(User user);

    /**
     * Add a user to the user-role registry with the specified role
     *
     * @param user  User to be added
     * @param role  Role for the user
     * @return true if user was successfully added to the registry, false otherwise
     */
    boolean addUser(User user, Role role);

    /**
     * Add a user to the user registry with the specified collection of roles
     *
     * @param user  User to be added
     * @param roles  Collection of roles for the user
     * @return true if user was successfully added to the registry, false otherwise
     */
    boolean addUser(User user, Collection<Role> roles);

    /**
     * Remove a user from the user-role registry
     *
     * @param user  User to be removed
     * @return true if user was successfully removed from the registry, false otherwise
     */
    boolean removeUser(User user);


    /**
     * Get the collection of all users in the user-role registry
     *
     * @return Collection of users
     */
    Collection<User> getUsers();

    /**
     * Check if a given user has the specified role
     *
     * @param user  User
     * @param role  Role
     * @return true if user has specified role, false otherwise
     */
    boolean hasRole(User user, Role role);

    /**
     * Get the collection of all roles associated with a user
     *
     * @param user  User
     * @return Collection of roles the user has
     */
    Collection<Role> getRoles(User user);

    /**
     * Add a role for the given user to user-role registry
     *
     * @param role  Role
     * @param user  User
     * @return true if role was successfully added to user's role registry, false otherwise
     */
    boolean addRole(Role role, User user);

    /**
     * Remove a role from the given user's role registry
     *
     * @param role  Role
     * @param user  User
     * @return true if role was successfully removed from user's role registry, false otherwise
     */
    boolean removeRole(Role role, User user);

}
