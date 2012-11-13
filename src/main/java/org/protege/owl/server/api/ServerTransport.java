package org.protege.owl.server.api;

import java.io.IOException;

public interface ServerTransport {
	
    /**
     * Starts the transport mechanism.
     * <p/>
     * This is used in conjuction with the Server.setTransports function.  The right sequence is
     * <pre>
     *        transport.start(server);
     *        server.setTransports(transports);
     * </pre>
     * 
     * @param server
     * @throws IOException
     */
	void start(Server server) throws IOException;
	
	/**
	 * Disposes the transport mechanism.
	 * <p>
	 * This is taken care of by the server shutdown implementation.
	 */
	void dispose();

}
