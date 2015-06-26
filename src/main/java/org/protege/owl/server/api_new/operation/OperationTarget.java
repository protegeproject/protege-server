package org.protege.owl.server.api_new.operation;


/**
 * An operation target represents the target of some operation, which can be, for example:
 * an OWL entity, or a Protégé UI element such as a tab
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface OperationTarget {

    /**
     * Get the operation target
     *
     * @return Operation target
     */
    Object getTarget();

}
