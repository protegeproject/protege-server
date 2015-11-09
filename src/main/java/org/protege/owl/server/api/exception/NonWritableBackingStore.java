package org.protege.owl.server.api.exception;


public class NonWritableBackingStore extends OWLServerException {
	private static final long serialVersionUID = -7599963185053848128L;

	public NonWritableBackingStore() {
	}
	
	public NonWritableBackingStore(String message) {
		super(message);
	}
	
	public NonWritableBackingStore(String message, Throwable t) {
		super(message, t);
	}
	
	public NonWritableBackingStore(Throwable t) {
		super(t);
	}

}
