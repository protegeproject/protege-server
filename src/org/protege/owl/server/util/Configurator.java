package org.protege.owl.server.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.api.ServerConnectionFactory;
import org.protege.owl.server.api.ServerFactory;
import org.protege.owl.server.configuration.ServerConfiguration;

/*
 * This is suitable for declarative services but the junit task uses him also.
 */

public class Configurator {
	private Logger logger = Logger.getLogger(Configurator.class);
    private ServerConfiguration configuration;
    private Set<ServerFactory> serverFactories = new HashSet<ServerFactory>();
    private Set<ServerConnectionFactory> connectionFactories = new HashSet<ServerConnectionFactory>();
    
    private ServerFactory currentServerFactory;
    private Server server;
    private ServerConnectionFactory currentConnectionFactory;
    private ServerConnection connection;
    
    public Configurator(ServerConfiguration configuration) {
    	this.configuration = configuration;
    }
    
    public Server getCurrentServer() {
        return server;
    }
    
    public void addServerFactory(ServerFactory factory) {
    	if (factory.isSuitable(configuration)) {
    		serverFactories.add(factory);
    		if (!isReady()) {
    			rebuild();
    		}
    	}
    }

    public void removeServerFactory(ServerFactory factory)  {
    	serverFactories.remove(factory);
    	if (currentServerFactory == factory && server != null) {
    		server.dispose();
    		server = null;
    		if (connection != null) {
    			connection.dispose();
    			connection = null;
    		}
    		rebuild();
    	}
    }
    
    public void addServerConnectionFactory(ServerConnectionFactory factory) {
    	if (factory.isSuitable(configuration)) {
    		connectionFactories.add(factory);
    		if (!isReady()) {
    			rebuild();
    		}
    	}
    }
    
    public void removeServerConnectionFactory(ServerConnectionFactory factory) {
    	connectionFactories.remove(factory);
    	if (currentConnectionFactory == factory && connection != null) {
    		connection.dispose();
    		connection = null;
    		rebuild();
    	}
    }
    
	public void start() {
		rebuild();
	}
	
	public void stop() {
		if (connection != null) {
			connection.dispose();
			connection = null;
		}
		server = null;
	}
	
    
    public boolean isReady() {
        return server != null && connection != null;
    }
    
    private void rebuild() {
    	try {
    		if (server == null) {
    			for (ServerFactory factory : serverFactories) {
    				currentServerFactory = factory;
    				server = factory.createServer(configuration);
    				if (server != null) {
    					break;
    				}
    			}
    		}
    		if (server != null && connection == null) {
    			for (ServerConnectionFactory factory : connectionFactories) {
    				currentConnectionFactory = factory;
    				connection = factory.createServerConnection(configuration);
    				connection.initialize(server);
    			}
    		}
    	}
    	catch (Throwable t) {
    		ServerConnection tmp = connection;
    		connection = null;
    		server = null;
    		logger.warn("Exception caught trying to configure server ", t);
    		if (tmp != null) {
    			tmp.dispose();
    		}
    	}
    }
}
