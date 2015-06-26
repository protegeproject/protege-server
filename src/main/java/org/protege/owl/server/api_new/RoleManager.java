package org.protege.owl.server.api_new;

import java.util.Set;

import org.protege.owl.server.api.User;
import org.protege.owl.server.api_new.operation.*;

/**
 * A manager for roles played by users
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface RoleManager {

    /**
     * Add the specified role
     *
     * @param role  Role
     */
    void addRole(Role role);

    /**
     * Remove a given role
     *
     * @param role  Role
     */
    void removeRole(Role role);

    /**
     * Get the set of all roles
     *
     * @return Set of existing roles
     */
    Set<Role> getRoles();

    /**
     * Get the set of roles within a given project
     *
     * @param project   Project
     * @return Set of roles within a project
     */
    Set<Role> getRoles(Project project);

    /**
     * Get the set of roles played by a specified user within a given project
     *
     * @param project   Project
     * @param user  User
     * @return Set of roles
     */
    Set<Role> getRoles(Project project, User user);

    /**
     * Get the set of roles played by the given user in all projects
     *
     * @param user  User
     * @return Set of roles
     */
    Set<Role> getRoles(User user);

    /**
     * Get the set of roles that contain a specified operation
     *
     * @param operation Operation
     * @return Set of roles containing specified operation
     */
    Set<Role> getRolesContainingOperation(Operation operation);

}
