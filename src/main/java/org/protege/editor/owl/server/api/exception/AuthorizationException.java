package org.protege.editor.owl.server.api.exception;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AuthorizationException extends OWLServerException {

    private static final long serialVersionUID = 101662876369152470L;

    public AuthorizationException() {
        super();
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(Throwable t) {
        super(t);
    }

    public AuthorizationException(String message, Throwable t) {
        super(message, t);
    }
}
