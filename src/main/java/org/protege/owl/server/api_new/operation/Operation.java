package org.protege.owl.server.api_new.operation;

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
     * Get the description of this operation
     *
     * @return Description of operation
     */
    String getDescription();

    /**
     * Get the operation target object
     *
     * @return Operation target object
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

}
