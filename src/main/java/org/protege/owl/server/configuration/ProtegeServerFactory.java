package org.protege.owl.server.configuration;

import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerFactory;
import org.protege.owl.server.api.server.ServerTransport;
import org.protege.owl.server.connect.rmi.RMITransport;
import org.protege.owl.server.core.ProtegeServer;
import org.protege.owl.server.policy.AccessControlFilter;
import org.protege.owl.server.security.AuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.protege.metaproject.api.Host;
import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * Responsible for building the complete Protege server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ProtegeServerFactory implements ServerFactory {

    @Override
    public Server build(ServerConfiguration configuration) {
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

    private Server injectServerTransport(Server server, ServerConfiguration configuration) {
        List<ServerTransport> transports = createServerTransports(configuration);
        server.setTransports(transports);
        return server;
    }

    private List<ServerTransport> createServerTransports(ServerConfiguration configuration) {
        Host host = configuration.getHost();
        int rmiRegistryPort = host.getRegistryPort().get();
        int serverPort = host.getPort().get();
        
        List<ServerTransport> transports = new ArrayList<>();
        transports.add(new RMITransport(rmiRegistryPort, serverPort));
        return transports;
    }
}
