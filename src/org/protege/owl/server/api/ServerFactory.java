package org.protege.owl.server.api;

import org.protege.owl.server.configuration.ServerConfiguration;

public interface ServerFactory {
    boolean hasSuitableConflictManager(ServerConfiguration configuration);
    
    ConflictManager createConflictManager(ServerConfiguration configuration);
    
    
    
    boolean hasSuitableConnection(ServerConfiguration configuration);

    ServerConnection createServerConnection(ServerConfiguration configuration);
    
    
	boolean hasSuitableServer(ServerConfiguration configuration);
	
    Server createServer(ServerConfiguration configuration);
    
}
