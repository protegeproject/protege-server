package org.protege.owl.server.api;

import java.io.IOException;

/**
 * Implementations of this class are responsible for making the server capabilities available on the 
 * web or internet.
 * 
 * @author tredmond
 *
 */

public interface ServerConnection {

    void initialize(Server server) throws IOException;
    
    Object getUserToken();
    
    void dispose();
}
