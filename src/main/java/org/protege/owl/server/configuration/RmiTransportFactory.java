package org.protege.owl.server.configuration;

import org.protege.owl.server.api.TransportFactory;
import org.protege.owl.server.api.TransportHandler;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.RmiTransport;

import java.net.URI;

import edu.stanford.protege.metaproject.api.Host;
import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class RmiTransportFactory implements TransportFactory {

    @Override
    public TransportHandler build(ServerConfiguration configuration) throws OWLServerException {
        Host host = configuration.getHost();
        URI remoteUri = host.getUri();
        int registryPort = host.getSecondaryPort().isPresent() ? host.getSecondaryPort().get().get() : remoteUri.getPort();
        int serverPort = remoteUri.getPort();
        return new RmiTransport(registryPort, serverPort);
    }
}
