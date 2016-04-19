package org.protege.editor.owl.server.api.exception;

public class OWLServerException extends Exception {
    private static final long serialVersionUID = 688550419032901597L;

    public OWLServerException() {
    }
    
    public OWLServerException(String message) {
        super(message);
    }

    public OWLServerException(Throwable t) {
        super(t);
    }
    
    public OWLServerException(String message, Throwable t) {
        super(message, t);
    }
}
