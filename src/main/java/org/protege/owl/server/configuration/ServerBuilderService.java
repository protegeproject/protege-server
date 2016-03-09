package org.protege.owl.server.configuration;

import org.protege.owl.server.api.server.BuilderService;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * Represents a service that is responsible for building the server in OSGi environment.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ServerBuilderService implements BuilderService {

    private final static Logger logger = LoggerFactory.getLogger(ServerBuilderService.class);

    private ServerFactory serverFactory;

    private Server server;

    private ServerConfiguration configuration;

    @Override
    public void initialize(ServerConfiguration configuration) {
        this.configuration = configuration;
        logger.info("Loading server configuration");
    }

    @Override
    public void setServerFactory(ServerFactory factory) {
        serverFactory = factory;
        server = serverFactory.build(configuration);
    }

    @Override
    public void removeServerFactory(ServerFactory factory) {
        if (factory == null) return;
        if (serverFactory.equals(factory)) {
            serverFactory = null;

            logger.info("Shutting down the server");
            server.shutdown();
            server = null;
        }
    }
}
