package org.protege.owl.server.api_new;

import org.protege.owl.server.api.User;
import org.protege.owl.server.api_new.operation.Operation;

import java.util.Set;

/**
 * A policy manager
 *
 * @author Rafael Gonçalves
 * Stanford Center for Biomedical Informatics Research
 */
public interface PolicyManager {

    /**
     * Add a policy
     *
     * @param policy    New policy
     */
    void addPolicy(Policy policy);

    /**
     * Remove a policy
     *
     * @param policy    Policy to be removed
     */
    void removePolicy(Policy policy);

    /**
     * Get all policies
     *
     * @return Set of existing policies
     */
    Set<Policy> getPolicies();

    /**
     * Check whether the specified operation is allowed for the given user
     *
     * @param operation Operation
     * @param project   Project
     * @param user  User
     * @return true if user is allowed to carry out the specified operation, false otherwise
     */
    boolean isOperationAllowed(Operation operation, Project project, User user);

    /**
     * Get the set of allowed operations for the specified user
     *
     * @param project   Project
     * @param user  User
     * @return Set of allowed operations for the specified user
     */
    Set<Operation> getAllowedOperations(Project project, User user);

    /**
     * Get all known operations
     *
     * @return Set of operations
     */
    Set<Operation> getAllOperations();

}
