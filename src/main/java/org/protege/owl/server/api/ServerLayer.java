package org.protege.owl.server.api;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

public interface ServerLayer extends Server {

    /**
     * Get the server configuration
     *
     * @return Server configuration
     */
    ServerConfiguration getConfiguration();
}
