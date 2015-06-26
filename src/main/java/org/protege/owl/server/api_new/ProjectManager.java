package org.protege.owl.server.api_new;

import java.util.Set;

/**
 * A project manager
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface ProjectManager {

    /**
     * Add a project to the project registry
     *
     * @param project   New project
     */
    void addProject(Project project);

    /**
     * Remove a project from the project registry
     *
     * @param project   Project to be removed
     */
    void removeProject(Project project);

    /**
     * Get a project by its name
     *
     * @param projectName   Project name
     * @return Project instance
     */
    Project getProject(String projectName);

    /**
     * Get the set of all projects
     *
     * @return Set of projects
     */
    Set<Project> getProjects();

}
