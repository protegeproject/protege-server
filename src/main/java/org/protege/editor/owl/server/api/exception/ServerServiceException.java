package org.protege.editor.owl.server.api.exception;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ServerServiceException extends OWLServerException {

    private static final long serialVersionUID = -366523715475334812L;

    public ServerServiceException() {
        // NO-OP
    }

    public ServerServiceException(String message) {
        super(message);
    }

    public ServerServiceException(Throwable t) {
        super(t);
    }

    public ServerServiceException(String message, Throwable t) {
        super(message, t);
    }
}
