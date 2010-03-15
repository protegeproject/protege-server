package org.protege.owl.server.api;

import java.util.List;
import java.util.Set;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public interface ConflictManager {
    void validateChanges(Set<RemoteOntology> versions, List<OWLOntologyChange> changes) throws RemoteOntologyChangeException;
}
