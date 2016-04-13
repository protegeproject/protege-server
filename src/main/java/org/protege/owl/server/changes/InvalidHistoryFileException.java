package org.protege.owl.server.changes;

import org.protege.owl.server.api.exception.OWLServerException;

public class InvalidHistoryFileException extends OWLServerException {

    private static final long serialVersionUID = -8781425021352696596L;

    public InvalidHistoryFileException(String message) {
        super(message);
    }
}
