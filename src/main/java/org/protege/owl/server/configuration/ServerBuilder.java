package org.protege.owl.server.configuration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.server.api.ConflictManager;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.api.ServerFactory;

/**
 * This class focuses on building exactly one server that meets a specification.  The 
 * specification in question is given by an OWL individual representing a server in the
 * metaproject.  The metaproject contains information that constrains how the server 
 * corresponding to this individual can be built and the Server builder tries to meet these
 * requirements.
 * 
 * @author tredmond
 *
 */
public class ServerBuilder {
	public static final Logger LOGGER = Logger.getLogger(ServerBuilder.class);
    private ServerConfiguration configuration;
    private Set<ServerFactory> serverFactories = new HashSet<ServerFactory>();
    
    private ServerFactory currentServerFactory;
    private Server server;
    private ServerFactory currentConnectionFactory;
    private ServerConnection connection;
    private ConflictManager conflictManager;
    private ServerFactory currentConflictFactory;
    
    public ServerBuilder(ServerConfiguration configuration) {
        LOGGER.info("Trying to configure server " + configuration);
        this.configuration = configuration;
        rebuild();
    }
    
    public Server getCurrentServer() {
        return server;
    }
    
    public synchronized void addServerFactory(ServerFactory factory) {
        serverFactories.add(factory);
        rebuild();
    }

    public synchronized void removeServerFactory(ServerFactory factory)  {
    	serverFactories.remove(factory);
    	boolean needsRebuild = false;
    	if (factory == currentServerFactory) {
    	    disableBackend();
    	    disableConflict();
    	    disableConnection();
            needsRebuild = true;
    	}
        else if (factory == currentConflictFactory) {
            disableConflict();
            disableConnection();
            needsRebuild = true;
        }
    	else if (factory == currentConnectionFactory) {
    	    disableConnection();
    		needsRebuild = true;
    	}
    	else {
    		needsRebuild = false;
    	}
    	if (needsRebuild) {
    	    LOGGER.info("Server shutdown.  Awaiting restart.");
    	    rebuild();
    	}
    }
    

	public void start() {
		rebuild();
	}
	
	public void stop() {
	    disableConnection();
	    disableConflict();
	    disableBackend();
	}
	
    
    public boolean isReady() {
        return server != null && connection != null;
    }
    
    private void rebuild() {
        if (configuration == null) {
            return;
        }
    	try {
    		if (server == null) {
                tryToEnableBackend();
    		}
            if (server != null && conflictManager == null) {
                tryToEnableConflict();
            }
    		if (server != null && conflictManager != null &&  connection == null) {
                tryToEnableConnection();
    		}
    	}
    	catch (Throwable t) {
    		ServerConnection tmp = connection;
    		server = null;
    		currentServerFactory = null;
    		connection = null;
    		currentConnectionFactory = null;
    		conflictManager = null;
    		currentConflictFactory = null;
    		LOGGER.warn("Exception caught trying to configure server ", t);
    		if (tmp != null) {
    			tmp.dispose();
    		}
    	}
    }

    private synchronized void tryToEnableBackend() {
        for (ServerFactory factory : serverFactories) {
            if (factory.hasSuitableServer(configuration)) {
                server = factory.createServer(configuration);
                currentServerFactory = factory;
                LOGGER.info("Matched " + configuration + " with server backend.");
                break;
            }
        }
    }

    private void disableBackend() {
        if (server != null) server.dispose();
        server = null;
        currentServerFactory = null;
    }
    
    private synchronized void tryToEnableConflict() {
        for (ServerFactory factory : serverFactories) {
            if (factory.hasSuitableConflictManager(configuration)) {
                conflictManager = factory.createConflictManager(configuration);
                conflictManager.initialise(server);
                server.setConflictManager(conflictManager);
                currentConflictFactory = factory;
                LOGGER.info("Matched " + configuration + " with conflict manager.");
                LOGGER.info("Server started.");
                break;
                        
            }
        }
    }

    private void disableConflict() {
        conflictManager = null;
        currentConflictFactory = null;
        if (server != null) {
            server.setConflictManager(null);
        }
    }


    private synchronized void tryToEnableConnection() throws IOException {
        for (ServerFactory factory : serverFactories) {
            if (factory.hasSuitableConnection(configuration)) {
                connection = factory.createServerConnection(configuration);
                connection.initialize(server);
                currentConnectionFactory = factory;
                LOGGER.info("Matched " + configuration + " with connection manager.");
                break;
            }
        }
    }
    
    private void disableConnection() {
        if (connection != null) connection.dispose();
        connection = null;
        currentConnectionFactory = null;
    }

}
