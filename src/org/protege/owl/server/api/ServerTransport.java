package org.protege.owl.server.api;

import java.io.IOException;

public interface ServerTransport {

    void initialize(Server server) throws IOException;
}
