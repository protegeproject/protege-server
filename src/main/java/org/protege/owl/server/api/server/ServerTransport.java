package org.protege.owl.server.api.server;

import java.io.IOException;

@Deprecated
public interface ServerTransport {
	
    /**
     * Starts the transport mechanism.
     * <p>
     * This is used in conjuction with the Server.setTransports function.  The right sequence is
     * <pre>
     *        transport.start(server);
     *        server.setTransports(transports);
     * </pre>
     * 
     * @param server	server
     * @throws IOException	IOException
     */
	void start(Server server) throws IOException;
	
	/**
	 * Disposes the transport mechanism.
	 * <p>
	 * This is taken care of by the server shutdown implementation.
	 */
	void dispose();

}
