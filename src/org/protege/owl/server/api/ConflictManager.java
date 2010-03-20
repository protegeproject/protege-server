package org.protege.owl.server.api;

import java.util.List;
import java.util.Map;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public interface ConflictManager {
    void validateChanges(Map<IRI, Integer> versions, List<OWLOntologyChange> changes) throws RemoteOntologyChangeException;
}
