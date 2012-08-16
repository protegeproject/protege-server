package org.protege.owl.server.api.exception;

import java.io.IOException;

public class DocumentHasWrongTypeException extends OWLServerException {
	private static final long serialVersionUID = -3582077474042337256L;

	public DocumentHasWrongTypeException() {
	}
	
	public DocumentHasWrongTypeException(String message) {
		super(message);
	}

	public DocumentHasWrongTypeException(Throwable t) {
		super(t);
	}
	
	public DocumentHasWrongTypeException(String message, Throwable t) {
		super(message, t);
	}
}

