package org.protege.editor.owl.server.security;

import java.io.IOException;

public class SSLContextInitializationException extends IOException {

	private static final long serialVersionUID = 7388040683023487104L;

	public SSLContextInitializationException() {
		super();
	}

	public SSLContextInitializationException(String message) {
		super(message);
	}

	public SSLContextInitializationException(Throwable t) {
		super(t);
	}

	public SSLContextInitializationException(String message, Throwable t) {
		super(message, t);
	}
}
