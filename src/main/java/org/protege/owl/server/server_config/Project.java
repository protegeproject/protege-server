package org.protege.owl.server.server_config;

import org.protege.owl.server.api.User;

import java.util.Set;

/**
 * A representation of a project
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface Project {

    /**
     * Get the name of the project
     *
     * @return Name of project
     */
    String getName();

    /**
     * Set the name of the project
     *
     * @param projectName   Project name
     */
    void setName(String projectName);

    /**
     * Get the owners of the project
     *
     * @return Set of users that own the project
     */
    Set<User> getOwners();

    /**
     * Get the administrators of the project
     *
     * @return Set of users that administrate the project
     */
    Set<User> getAdministrators();

    /**
     * Set new owners for the project
     *
     * @param owners    New set of owners
     */
    void setOwners(Set<User> owners);

    /**
     * Add a project-owning user
     *
     * @param user  Owner user to be added
     */
    void addOwner(User user);

    /**
     * Remove a project-owner user
     *
     * @param user  Owner user to be removed
     */
    void removeOwner(User user);

    /**
     * Set new administrators for the project
     *
     * @param administrators    New set of administrators
     */
    void setAdministrators(Set<User> administrators);

    /**
     * Add an administrator of the project
     *
     * @param user  Administrator user to be added
     */
    void addAdministrator(User user);

    /**
     * Remove an admininstrator of the project
     *
     * @param user  Administrator user to be removed
     */
    void removeAdministrator(User user);

}
