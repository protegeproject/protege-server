package org.protege.owl.server.api.server;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface Server extends ServerExports, ServerRequests, ServerInternals {

    /**
     * Get the server configuration
     *
     * @return Server configuration
     */
    ServerConfiguration getConfiguration();
}
