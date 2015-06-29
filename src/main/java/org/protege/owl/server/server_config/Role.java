package org.protege.owl.server.server_config;

import org.protege.owl.server.server_config.operation.*;

import java.util.Set;


/**
 * A role defines a group of allowed operations within some project
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface Role {

    /**
     * Get the role name
     *
     * @return Role name
     */
    String getName();

    /**
     * Set the name of the role
     *
     * @param roleName  Role name
     */
    void setName(String roleName);

    /**
     * Add a project to this role
     *
     * @param project   Project
     */
    void addProject(Project project);

    /**
     * Remove a project from this role
     *
     * @param project   Project
     */
    void removeProject(Project project);

    /**
     * Get all projects associated with this role
     *
     * @return Set of projects
     */
    Set<Project> getProjects();

    /**
     * Get the set of allowed operations
     *
     * @return Set of operations
     */
    Set<Operation> getOperations();

    /**
     * Add an operation to this role
     *
     * @param operation Operation to be added
     */
    void addOperation(Operation operation);

    /**
     * Add the specified operations to the set of allowed operations
     *
     * @param operations    Operations to be added
     */
    void addOperations(Set<Operation> operations);

    /**
     * Remove an operation from this role
     *
     * @param operation Operation to be removed
     */
    void removeOperation(Operation operation);

    /**
     * Remove the specified operations from the set of allowed operations
     *
     * @param operations    Operations to be removed
     */
    void removeOperations(Set<Operation> operations);

    /**
     * Replace the set of allowed operations with the given one
     *
     * @param operations    New set of allowed operations
     */
    void setOperations(Set<Operation> operations);

    /**
     * Replace the set of projects with the given one
     *
     * @param projects  New set of projects
     */
    void setProjects(Set<Project> projects);

}
