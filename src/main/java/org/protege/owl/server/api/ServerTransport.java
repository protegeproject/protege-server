package org.protege.owl.server.api;

import java.io.IOException;

public interface ServerTransport {
	
	void start(Server server) throws IOException;
	
	void dispose();

}
