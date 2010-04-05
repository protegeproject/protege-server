package org.protege.owl.server.api;

import org.protege.owl.server.configuration.ServerConfiguration;

public interface ServerConnectionFactory {
	boolean isSuitable(ServerConfiguration serverConfiguration);

    ServerConnection createServerConnection(ServerConfiguration serverConfiguration);

}
