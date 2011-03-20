package org.protege.owl.server.exception;

public class RemoteOntologyChangeException extends RemoteOntologyException {
    private static final long serialVersionUID = 1666055672451705893L;

    public RemoteOntologyChangeException() {
    }
    
    public RemoteOntologyChangeException(String message) {
        super(message);
    }
    
    public RemoteOntologyChangeException(String message, Throwable t) {
        super(message, t);
    }
    
    public RemoteOntologyChangeException(Throwable t) {
        super(t);
    }
}
