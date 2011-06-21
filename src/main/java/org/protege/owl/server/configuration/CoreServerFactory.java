package org.protege.owl.server.configuration;

import org.protege.owl.server.api.ConflictManager;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.api.ServerFactory;
import org.protege.owl.server.conflict.StrictConflictManager;

/**
 * This factory will supply all the components that are implemented in the core server
 * api.  Probably over time this will just consist of conflict managers.
 * 
 * @author tredmond
 *
 */
public class CoreServerFactory implements ServerFactory {

    @Override
    public boolean hasSuitableServer(ServerConfiguration configuration) {
        return false;
    }

    @Override
    public Server createServer(ServerConfiguration configuration) {
        return null;
    }

    @Override
    public boolean hasSuitableConnection(ServerConfiguration configuration) {
        return false;
    }

    @Override
    public ServerConnection createServerConnection(ServerConfiguration configuration) {
        return null;
    }

    @Override
    public boolean hasSuitableConflictManager(ServerConfiguration configuration) {
        return StrictConflictConfiguration.isSuitable(configuration);
    }

    @Override
    public ConflictManager createConflictManager(ServerConfiguration configuration) {
        return new StrictConflictManager();
    }
    
    @Override
    public String toString() {
    	return "Core Conflict Managers Factory";
    }

}
