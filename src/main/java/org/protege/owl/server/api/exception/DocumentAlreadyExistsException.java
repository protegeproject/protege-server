package org.protege.owl.server.api.exception;

import java.io.IOException;

public class DocumentAlreadyExistsException extends OWLServerException {
	private static final long serialVersionUID = -3582077474042337256L;

	public DocumentAlreadyExistsException() {
	}
	
	public DocumentAlreadyExistsException(String message) {
		super(message);
	}

	public DocumentAlreadyExistsException(Throwable t) {
		super(t);
	}
	
	public DocumentAlreadyExistsException(String message, Throwable t) {
		super(message, t);
	}
}

