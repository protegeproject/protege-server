package org.protege.editor.owl.server.policy;

import org.protege.editor.owl.server.api.exception.ServerServiceException;

/**
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class OperationForChangeNotFoundException extends ServerServiceException {

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
