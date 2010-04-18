package org.protege.owl.server.exception;

public class RuntimeServerException extends RuntimeException {
    private static final long serialVersionUID = -8304047400109142176L;

    public RuntimeServerException() {
    }
    
    public RuntimeServerException(String message) {
        super(message);
    }
    
    public RuntimeServerException(String message, Throwable t) {
        super(message, t);
    }
    
    public RuntimeServerException(Throwable t) {
        super(t);
    }
}
