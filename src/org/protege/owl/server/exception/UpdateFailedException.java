package org.protege.owl.server.exception;

import org.semanticweb.owlapi.model.OWLOntologyChangeException;

public class UpdateFailedException extends OWLOntologyChangeException {
    private static final long serialVersionUID = 8623073958341408974L;

    public UpdateFailedException(Throwable t) {
        super(null, t);
    }
}
