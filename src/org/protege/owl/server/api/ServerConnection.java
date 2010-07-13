package org.protege.owl.server.api;

import java.io.IOException;

/**
 * Implementations of this class are responsible for making the server capabilities available on the 
 * web or internet.  There may be several variants of this class implementing restful services, rmi.  Currently
 * we have a restful service implementation but we hope to eventually have a  version based on OWLLink.
 * 
 * @author tredmond
 *
 */

public interface ServerConnection {

    void initialize(Server server) throws IOException;
    
    /**
     * Not used yet.
     * @return a token indicating a specific user  connection to the server.
     */
    Object getUserToken();
    
    void dispose();
}
