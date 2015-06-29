package org.protege.owl.server.server_config.operation;

/**
 * Requirements for an operation (e.g., existence of some class)
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface OperationRequirements {

    /**
     * Get operation requirements
     *
     * @return Operation requirements
     */
    Object getRequirements();

}
