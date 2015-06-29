package org.protege.owl.server.server_config.operation;

import com.google.common.base.Optional;

/**
 * An operation is associated with an operation target
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface Operation {

    /**
     * Get the operation name
     *
     * @return Operation name
     */
    String getName();

    /**
     * Get the description of the operation
     *
     * @return Description of operation
     */
    String getDescription();

    /**
     * Get the requirements for the operation
     *
     * @return Operation requirement instance
     */
    Optional<OperationRequirements> getRequirements();

    /**
     * Get the operation target
     *
     * @return Operation target instance
     */
    OperationTarget getTarget();

    /**
     * Set a new operation name
     *
     * @param operationName New operation name
     */
    void setName(String operationName);

    /**
     * Set a new operation description
     *
     * @param operationDescription  New operation description
     */
    void setDescription(String operationDescription);

    /**
     * Set a new operation target
     *
     * @param operationTarget   New operation target
     */
    void setTarget(OperationTarget operationTarget);

    /**
     * Set the requirements for the operation
     *
     * @param requirements  Operation requirement instance
     */
    void setRequirements(OperationRequirements requirements);

}
