package org.protege.owl.server.exception;

import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyChange;


public class RemoteOntologyChangeException extends RemoteOntologyException {
    private static final long serialVersionUID = -1046543185411030949L;
    private List<OWLOntologyChange> rejectedChanges;
    
    public RemoteOntologyChangeException(List<OWLOntologyChange> rejectedChanges) {
        this.rejectedChanges = rejectedChanges;
    }
    
    public RemoteOntologyChangeException(List<OWLOntologyChange> rejectedChanges, Throwable cause) {
        super(cause);
        this.rejectedChanges = rejectedChanges; 
    }

    public RemoteOntologyChangeException(List<OWLOntologyChange> rejectedChanges, String message, Throwable cause) {
        super(message, cause);
        this.rejectedChanges = rejectedChanges; 
    }
    
    public List<OWLOntologyChange> getRejectedChanges() {
        return rejectedChanges;
    }
}
