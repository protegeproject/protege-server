package org.protege.editor.owl.server.api.exception;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class OutOfSyncException extends OWLServerException {

    private static final long serialVersionUID = 332790751711504336L;

    public OutOfSyncException() {
        // NO-OP
    }

    public OutOfSyncException(String message) {
        super(message);
    }

    public OutOfSyncException(Throwable t) {
        super(t);
    }

    public OutOfSyncException(String message, Throwable t) {
        super(message, t);
    }
}
