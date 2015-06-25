package org.protege.owl.server.api_new;

import com.google.common.base.Objects;
import org.protege.owl.server.api_new.operation.*;

import java.util.Set;


/**
 * An instance of Role associates some role (name) to a group of allowed operations
 *
 * @author Rafael Gonçalves
 * Stanford Center for Biomedical Informatics Research
 */
public class Role {
    private String roleName;
    private Set<Operation> operations;

    /**
     * Constructor
     *
     * @param roleName  Name of the role
     * @param operations    Set of allowed operations
     */
    public Role(String roleName, Set<Operation> operations) {
        this.roleName = roleName;
        this.operations = operations;
    }

    /**
     * Get the role name
     *
     * @return Role name
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Get the set of allowed operations
     *
     * @return Set of operations
     */
    public Set<Operation> getOperations() {
        return operations;
    }

    /**
     * Add an operation to this role
     *
     * @param operation Operation to be added
     */
    public void addOperation(Operation operation) {
        operations.add(operation);
    }

    /**
     * Add the specified operations to the set of allowed operations
     *
     * @param operations    Operations to be added
     */
    public void addOperations(Set<Operation> operations) {
        this.operations.addAll(operations);
    }

    /**
     * Remove an operation from this role
     *
     * @param operation Operation to be removed
     */
    public void removeOperation(Operation operation) {
        operations.remove(operation);
    }

    /**
     * Remove the specified operations from the set of allowed operations
     *
     * @param operations    Operations to be removed
     */
    public void removeOperations(Set<Operation> operations) {
        this.operations.removeAll(operations);
    }

    /**
     * Set the name of this role
     *
     * @param roleName  New role name
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Replace the set of allowed operations with the given one
     *
     * @param operations    New set of allowed operations
     */
    public void setOperations(Set<Operation> operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("roleName", roleName)
                .add("operations", operations)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equal(roleName, role.roleName) &&
                Objects.equal(operations, role.operations);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roleName, operations);
    }

}
