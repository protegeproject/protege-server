package org.protege.owl.server.exception;

import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyChange;

public class RuntimeOntologyConflictException extends RuntimeException {
    private static final long serialVersionUID = 793767137267203642L;
    private List<OWLOntologyChange> rejectedChanges;
    
    public RuntimeOntologyConflictException(List<OWLOntologyChange> rejectedChanges) {
        this.rejectedChanges = rejectedChanges;
    }
    
    public RuntimeOntologyConflictException(List<OWLOntologyChange> rejectedChanges, Throwable cause) {
        super(cause);
        this.rejectedChanges = rejectedChanges; 
    }

    public RuntimeOntologyConflictException(List<OWLOntologyChange> rejectedChanges, String message, Throwable cause) {
        super(message, cause);
        this.rejectedChanges = rejectedChanges; 
    }
    
    public List<OWLOntologyChange> getRejectedChanges() {
        return rejectedChanges;
    }
}
