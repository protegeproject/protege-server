package org.protege.owl.server.exception;

import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyChange;


public class OntologyConflictException extends RemoteOntologyChangeException {
    private static final long serialVersionUID = -1046543185411030949L;
    private List<OWLOntologyChange> rejectedChanges;
    
    public OntologyConflictException(List<OWLOntologyChange> rejectedChanges) {
        this.rejectedChanges = rejectedChanges;
    }
    
    public OntologyConflictException(List<OWLOntologyChange> rejectedChanges, Throwable cause) {
        super(cause);
        this.rejectedChanges = rejectedChanges; 
    }

    public OntologyConflictException(List<OWLOntologyChange> rejectedChanges, String message, Throwable cause) {
        super(message, cause);
        this.rejectedChanges = rejectedChanges; 
    }
    
    public List<OWLOntologyChange> getRejectedChanges() {
        return rejectedChanges;
    }
}
