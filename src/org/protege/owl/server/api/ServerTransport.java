package org.protege.owl.server.api;

public interface ServerTransport {
	
	void start(Server server);
	
	void dispose();

}
