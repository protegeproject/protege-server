package org.protege.owl.server.exception;

public abstract class RemoteOntologyException extends Exception {
    private static final long serialVersionUID = 1408785892788573418L;

    protected RemoteOntologyException() {
    }
    
    protected RemoteOntologyException(String message) {
        super(message);
    }
    
    protected RemoteOntologyException(String message, Throwable t) {
        super(message, t);
    }
    
    protected RemoteOntologyException(Throwable t) {
        super(t);
    }
}
