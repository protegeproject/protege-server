package org.protege.owl.server.api;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

public abstract class ServerLayer implements Server {

    /**
     * Get the server configuration
     *
     * @return Server configuration
     */
    protected abstract ServerConfiguration getConfiguration();
}
