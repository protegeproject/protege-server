package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.api.exception.OWLServerException;

public class InvalidHistoryFileException extends OWLServerException {

    private static final long serialVersionUID = -8781425021352696596L;

    public InvalidHistoryFileException(String message) {
        super(message);
    }
}
