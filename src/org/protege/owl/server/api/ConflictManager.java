package org.protege.owl.server.api;

import java.util.List;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public interface ConflictManager {
    void validateChanges(List<OWLOntologyChange> changes) throws RemoteOntologyChangeException;
}
