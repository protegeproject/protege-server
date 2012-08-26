package org.protege.owl.server.api.exception;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyChange;

public class ConflictException extends OWLServerException {
    private static final long serialVersionUID = 4355727797287471775L;
    private Set<OWLOntologyChange> conflicts;
    
    private static String getConflictMessage(Set<OWLOntologyChange> conflicts) {
        if (conflicts.size() == 1) {
            return "Conflict found";
        }
        else {
            return "" + conflicts.size() + " conflicts found.";
        }
    }
    
    public ConflictException(Set<OWLOntologyChange> conflicts) {
        super(getConflictMessage(conflicts));
        this.conflicts = conflicts;
    }
    
    public Set<OWLOntologyChange> getConflicts() {
        return conflicts;
    }

}
