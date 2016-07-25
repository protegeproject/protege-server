package org.protege.editor.owl.server.http.exception;

import java.io.IOException;

public class ServerConfigurationInitializationException extends IOException {

	private static final long serialVersionUID = 4042907320620922752L;

	public ServerConfigurationInitializationException() {
		super();
	}

	public ServerConfigurationInitializationException(String message) {
		super(message);
	}

	public ServerConfigurationInitializationException(Throwable t) {
		super(t);
	}

	public ServerConfigurationInitializationException(String message, Throwable t) {
		super(message, t);
	}
}
