package org.protege.owl.server.configuration;

import org.protege.owl.server.api.BuilderService;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerFactory;
import org.protege.owl.server.api.TransportFactory;
import org.protege.owl.server.api.server.TransportHandler;

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

    private TransportFactory transportFactory;

    private ServerConfiguration configuration;

    @Override
    public void buildAndLaunchServer() {
        try {
            Server server = serverFactory.build(configuration);
            TransportHandler transport = transportFactory.build(configuration);
            server.setTransport(transport);
            transport.bind(server);
        }
        catch (Exception e) {
            logger.error("Failed to build the server instance", e);
        }
    }

    @Override
    public void initialize(ServerConfiguration configuration) {
        this.configuration = configuration;
        logger.info("Loading server configuration");
    }

    @Override
    public void setServerFactory(ServerFactory factory) {
        serverFactory = factory;
    }

    @Override
    public void setTransportFactory(TransportFactory factory) {
        transportFactory = factory;
    }
}
