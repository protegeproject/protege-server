package org.protege.owl.server.configuration;

import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerFactory;
import org.protege.owl.server.api.server.TransportHandler;
import org.protege.owl.server.connect.RmiTransport;
import org.protege.owl.server.core.ProtegeServer;
import org.protege.owl.server.policy.AccessControlFilter;
import org.protege.owl.server.security.AuthenticationFilter;

import edu.stanford.protege.metaproject.api.Host;
import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * Responsible for building the complete Protege server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public class ProtegeServerFactory implements ServerFactory {

    @Override
    public Server build(ServerConfiguration configuration) throws OWLServerException {
        Server server = addAccessControlLayer(createBaseServer(configuration));
        server = addAuthenticationLayer(server);
        server = injectServerTransport(server, configuration);
        return server;
    }

    private Server createBaseServer(ServerConfiguration configuration) {
        return new ProtegeServer(configuration);
    }

    private Server addAccessControlLayer(Server server) {
        return new AccessControlFilter(server);
    }

    private Server addAuthenticationLayer(Server server) {
        return new AuthenticationFilter(server);
    }

    private Server injectServerTransport(Server server, ServerConfiguration configuration)
            throws OWLServerException {
        TransportHandler transport = createTransport(configuration);
        server.setTransport(transport);
        return server;
    }

    private TransportHandler createTransport(ServerConfiguration configuration) {
        Host host = configuration.getHost();
        int registryPort = host.getRegistryPort().get();
        int serverPort = host.getPort().get();
        return new RmiTransport(registryPort, serverPort);
    }
}
