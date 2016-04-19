package org.protege.editor.owl.server.api.exception;

public class RuntimeOWLServerException extends RuntimeException {
    private static final long serialVersionUID = -2109419764343481686L;

    public RuntimeOWLServerException(OWLServerException ose) {
        super(ose);
    }
    
    @Override
    public OWLServerException getCause() {
        return (OWLServerException) super.getCause();
    }
}
