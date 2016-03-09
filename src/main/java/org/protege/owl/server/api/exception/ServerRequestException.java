package org.protege.owl.server.api.exception;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ServerRequestException extends OWLServerException {

    private static final long serialVersionUID = -366523715475334812L;

    public ServerRequestException() {
        // NO-OP
    }

    public ServerRequestException(String message) {
        super(message);
    }

    public ServerRequestException(Throwable t) {
        super(t);
    }

    public ServerRequestException(String message, Throwable t) {
        super(message, t);
    }
}
