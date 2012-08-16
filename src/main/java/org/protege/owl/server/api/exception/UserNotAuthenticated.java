package org.protege.owl.server.api.exception;


public class UserNotAuthenticated extends OWLServerException {
    private static final long serialVersionUID = -4133433454108805207L;

    public UserNotAuthenticated() {
        
    }
    
    public UserNotAuthenticated(Throwable t) {
        super(t);
    }
    
    public UserNotAuthenticated(String message) {
        super(message);
    }

    public UserNotAuthenticated(String message, Throwable t) {
        super(message, t);
    }
}
