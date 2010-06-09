package org.protege.owl.server.exception;

import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyChange;


public class OntologyConflictException extends RemoteOntologyChangeException {
    private static final long serialVersionUID = -1046543185411030949L;
    private List<OWLOntologyChange> rejectedChanges;
    
    public OntologyConflictException(List<OWLOntologyChange> rejectedChanges) {
        super(rejectedChanges.size() == 1 ? "One change rejected" : "" + rejectedChanges.size() + " changes rejected");
        this.rejectedChanges = rejectedChanges;
    }
    
    public OntologyConflictException(String message, List<OWLOntologyChange> rejectedChanges) {
        super(message);
        this.rejectedChanges = rejectedChanges;
    }
    
    public OntologyConflictException(Throwable cause, List<OWLOntologyChange> rejectedChanges) {
        super(cause);
        this.rejectedChanges = rejectedChanges; 
    }

    public OntologyConflictException(String message, Throwable cause, List<OWLOntologyChange> rejectedChanges) {
        super(message, cause);
        this.rejectedChanges = rejectedChanges; 
    }
    
    public List<OWLOntologyChange> getRejectedChanges() {
        return rejectedChanges;
    }
}
