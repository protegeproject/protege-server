package org.protege.owl.server.configuration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.server.api.ServerBuilder;
import org.protege.owl.server.api.ConflictManager;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerConnection;
import org.protege.owl.server.api.ServerFactory;
import org.protege.owl.server.metaproject.MetaProject;
import org.protege.owl.server.metaproject.Vocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/*
 * This is suitable for declarative services but the junit task uses him also.
 */

public class ServerBuilderImpl implements ServerBuilder {
	public static final Logger LOGGER = Logger.getLogger(ServerBuilderImpl.class);
    private ServerConfiguration configuration;
    private Set<ServerFactory> serverFactories = new HashSet<ServerFactory>();
    
    private ServerFactory currentServerFactory;
    private Server server;
    private ServerFactory currentConnectionFactory;
    private ServerConnection connection;
    private ConflictManager conflictManager;
    private ServerFactory currentConflictFactory;
    
    public ServerBuilderImpl(ServerConfiguration configuration) {
        LOGGER.info("Trying to configure server " + configuration);
        this.configuration = configuration;
        rebuild();
    }
    
    public Server getCurrentServer() {
        return server;
    }
    
    public void addServerFactory(ServerFactory factory) {
        serverFactories.add(factory);
        rebuild();
    }

    public void removeServerFactory(ServerFactory factory)  {
    	serverFactories.remove(factory);
    	boolean needsRebuild = false;
    	if (factory == currentServerFactory) {
    		server.dispose();
    		server = null;
    		currentServerFactory = null;
    		
    		if (connection != null) connection.dispose();
    		connection = null;
    		currentConnectionFactory = null;
    		
    		conflictManager = null;
    		currentConflictFactory = null;
    		
    		needsRebuild = true;
    	}
    	else if (factory == currentConnectionFactory) {
    		if (connection != null) connection.dispose();
    		connection = null;
    		currentConnectionFactory = null;
    		
    		conflictManager = null;
    		currentConflictFactory = null;
    		
    		needsRebuild = true;
    	}
    	else if (factory == currentConflictFactory) {
    		conflictManager = null;
    		currentConflictFactory = null;
    	}
    	else {
    		needsRebuild = false;
    	}
    	if (needsRebuild) rebuild();
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
        if (configuration == null) {
            return;
        }
    	try {
    		if (server == null) {
    		    for (ServerFactory factory : serverFactories) {
    		        if (factory.hasSuitableServer(configuration)) {
    		            server = factory.createServer(configuration);
    		            currentServerFactory = factory;
    		            LOGGER.info("Matched " + configuration + " with server backend.");
    		            break;
    		        }
    			}
    		}
    		if (server != null && connection == null) {
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
    		if (server != null && connection != null && conflictManager == null) {
    			for (ServerFactory factory : serverFactories) {
    				if (factory.hasSuitableConflictManager(configuration)) {
    					conflictManager = factory.createConflictManager(configuration);
    					conflictManager.initialise(server);
    					server.setConflictManager(conflictManager);
    					currentConflictFactory = factory;
    					LOGGER.info("Matched " + configuration + " with conflict manager.");
    					break;
    					
    				}
    			}
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
}
