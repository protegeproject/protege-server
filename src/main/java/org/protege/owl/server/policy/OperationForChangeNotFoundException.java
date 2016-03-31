package org.protege.owl.server.policy;

import org.protege.owl.server.api.exception.ServerRequestException;

/**
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class OperationForChangeNotFoundException extends ServerRequestException {

    private static final long serialVersionUID = -8928067718594065835L;

    public OperationForChangeNotFoundException() {
        super();
    }

    public OperationForChangeNotFoundException(String message) {
        super(message);
    }

    public OperationForChangeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationForChangeNotFoundException(Throwable cause) {
        super(cause);
    }
}
