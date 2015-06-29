package org.protege.owl.server.server_config.operation;

import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;

/**
 * An operation target instance that refers to an OWL entity, such as a class or property
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface OWLEntityOperationTarget extends OperationTarget {

    @Override
    OWLEntity getTarget();

    /**
     * Set the allowed vocabulary for the operation target
     *
     * @param vocabulary    Set of OWL entities
     */
    void setAllowedVocabulary(Set<OWLEntity> vocabulary);

    /**
     * Get the set of OWL entities that are possible targets of a change; that is, if a user
     * attempts to merge two classes, one of which is not in the allowed vocabulary, the
     * operation should not be allowed
     *
     * @return Set of OWL entities that can be operation targets
     */
    Set<OWLEntity> getAllowedVocabulary();

}
