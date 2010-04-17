package org.protege.owl.server.exception;

public class RemoteQueryException extends RemoteOntologyException {
	
	public RemoteQueryException() {
	}
	
	public RemoteQueryException(String msg) {
		super(msg);
	}

	public RemoteQueryException(Throwable t) {
		super(t);
	}
	
	public RemoteQueryException(String msg, Throwable t) {
		super(msg, t);
	}

}
