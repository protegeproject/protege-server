package org.protege.owl.server.connection.servlet;

public class RPCRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -5626805489265150643L;

    public RPCRuntimeException(Throwable t) {
        super(t);
    }
}
