package org.protege.owl.server.api.exception;

public class ServerException extends Exception {
    private static final long serialVersionUID = 688550419032901597L;

    public ServerException() {
    }
    
    public ServerException(String message) {
        super(message);
    }

    public ServerException(Throwable t) {
        super(t);
    }
    
    public ServerException(String message, Throwable t) {
        super(message, t);
    }
}
