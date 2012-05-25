package org.protege.owl.server.api;

public abstract class ServerFilter implements Server {
	private Server delegate;
	
	public ServerFilter(Server delegate) {
		this.delegate = delegate;
	}
	
	public Server getDelegate() {
		return delegate;
	}
	
}
