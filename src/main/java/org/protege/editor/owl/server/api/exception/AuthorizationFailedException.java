package org.protege.editor.owl.server.api.exception;

public class AuthorizationFailedException extends OWLServerException {
    private static final long serialVersionUID = 5738407719377300595L;

    public AuthorizationFailedException() {
    }
    
    public AuthorizationFailedException(String msg) {
        super(msg);
    }
    
    public AuthorizationFailedException(Throwable t) {
        super(t);
    }
    
    public AuthorizationFailedException(String message, Throwable t) {
        super(message, t);
    }
}
