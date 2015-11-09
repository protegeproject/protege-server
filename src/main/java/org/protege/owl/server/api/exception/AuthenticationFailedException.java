package org.protege.owl.server.api.exception;


public class AuthenticationFailedException extends OWLServerException {
    private static final long serialVersionUID = -4133433454108805207L;

    public AuthenticationFailedException() {
        
    }
    
    public AuthenticationFailedException(Throwable t) {
        super(t);
    }
    
    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable t) {
        super(message, t);
    }
}
