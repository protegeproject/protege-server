package org.protege.owl.server.exception;

public class InvalidRemoteDataException extends RemoteOntologyException {
	private static final long serialVersionUID = -5887132834634417436L;

	public InvalidRemoteDataException() {
	}
	
	public InvalidRemoteDataException(String msg) {
		super(msg);
	}

	public InvalidRemoteDataException(Throwable t) {
		super(t);
	}
	
	public InvalidRemoteDataException(String msg, Throwable t) {
		super(msg, t);
	}
}
