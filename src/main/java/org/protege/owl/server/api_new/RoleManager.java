package org.protege.owl.server.api_new;

import java.util.Collection;
import org.protege.owl.server.api_new.operation.*;

/**
 * A manager for roles played by users
 *
 * @author Rafael Gonçalves
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
     * Get the collection of all roles
     *
     * @return Collection of existing roles
     */
    Collection<Role> getRoles();

    /**
     * Get the collection of roles that contain a specified operation
     *
     * @param operation Operation
     * @return Collection of roles containing specified operation
     */
    Collection<Role> getRolesContainingOperation(Operation operation);

}
