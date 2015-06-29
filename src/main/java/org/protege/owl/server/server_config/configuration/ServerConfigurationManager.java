package org.protege.owl.server.server_config.configuration;

import org.protege.owl.server.api.User;
import org.protege.owl.server.server_config.Project;
import org.protege.owl.server.server_config.Role;
import org.protege.owl.server.server_config.configuration.idgeneration.TermIdentifierGenerationStrategy;

import java.util.Set;

/**
 * Manager for server-side configurations
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface ServerConfigurationManager extends ConfigurationManager {

    /**
     * Get the host name of the server
     *
     * @return Server host name
     */
    String getHostName();

    /**
     * Set the host name of the server
     *
     * @param hostName  Server host name
     */
    void setHostName(String hostName);

    /**
     * Get the set of all projects
     *
     * @return Set of projects
     */
    Set<Project> getProjects();

    /**
     * Get the set of all roles
     *
     * @return Set of roles
     */
    Set<Role> getRoles();

    /**
     * Get the set of all users
     *
     * @return Set of users
     */
    Set<User> getUsers();

    /**
     * Generate a new OWL class identifier
     *
     * @return New OWL class identifier
     */
    int getNewClassIdentifier();

    /**
     * Set the unique term identifier generation strategy
     *
     * @param strategy  Unique term identifier generation strategy
     */
    void setTermIdentifierGenerationStrategy(TermIdentifierGenerationStrategy strategy);

}
