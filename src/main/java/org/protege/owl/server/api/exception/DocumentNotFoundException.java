package org.protege.owl.server.api.exception;


public class DocumentNotFoundException extends OWLServerException {
	private static final long serialVersionUID = -3582077474042337256L;

	public DocumentNotFoundException() {
	}
	
	public DocumentNotFoundException(String message) {
		super(message);
	}

	public DocumentNotFoundException(Throwable t) {
		super(t);
	}
	
	public DocumentNotFoundException(String message, Throwable t) {
		super(message, t);
	}
}

