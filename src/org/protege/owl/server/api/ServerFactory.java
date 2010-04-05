package org.protege.owl.server.api;

import org.protege.owl.server.configuration.ServerConfiguration;

public interface ServerFactory {
	boolean isSuitable(ServerConfiguration serverConfiguration);
	
    Server createServer(ServerConfiguration serverConfiguration);
    
}
