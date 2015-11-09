package org.protege.owl.server.api.exception;

public class UserDeclinedAuthenticationException extends AuthenticationFailedException {
    private static final long serialVersionUID = -6875740262455666279L;

    public UserDeclinedAuthenticationException() {
    }
    
    public UserDeclinedAuthenticationException(String message) {
        super(message);
    }
    
    public UserDeclinedAuthenticationException(Throwable t) {
        super(t);
    }
    
    public UserDeclinedAuthenticationException(String message, Throwable t) {
        super(message, t);
    }
}
