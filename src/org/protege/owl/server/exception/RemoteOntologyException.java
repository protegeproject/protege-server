package org.protege.owl.server.exception;

public class RemoteOntologyException extends Exception {
    private static final long serialVersionUID = 1408785892788573418L;

    public RemoteOntologyException() {
    }
    
    public RemoteOntologyException(String message) {
        super(message);
    }
    
    public RemoteOntologyException(String message, Throwable t) {
        super(message, t);
    }
    
    public RemoteOntologyException(Throwable t) {
        super(t);
    }
}
