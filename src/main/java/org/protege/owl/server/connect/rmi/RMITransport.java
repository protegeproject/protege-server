package org.protege.owl.server.connect.rmi;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.connect.ConfigurableCompression;

/*
 * ToDo - add SslRMIServerSocketFactory and SSLRMIClientSocketFactory
 *        to make this work I need to setup some crypto as otherwise there are handshake failures.
 *        also I need to remember to add this to the guys that use the RMI transport for a back channel.
 *        I may be able to find these by looking at the callers of getServerPort() - I will know if this works shortly.
 */
public class RMITransport implements ServerTransport, ConfigurableCompression {
	public static final String SERVER_NAME = "OWL 2 Server";
	
	private Logger logger = Logger.getLogger(RMITransport.class.getCanonicalName());
	private RemoteServerImpl exportedServer;
	private Registry registry;
	private int rmiRegistryPort;
	private int serverPort;
	
	public RMITransport(int rmiRegistryPort, int serverPort) {
		this.rmiRegistryPort = rmiRegistryPort;
		this.serverPort = serverPort;
	}
	
	public int getRmiRegistryPort() {
        return rmiRegistryPort;
    }
	
	public int getServerPort() {
        return serverPort;
    }
	
	public Registry getRegistry() {
	    return registry;
	}
	
	@Override
	public void setCompressionLimit(int networkCompressionLimit) {
	    exportedServer.setNetworkCompressionLimit(networkCompressionLimit);
	}

	@Override
	public void start(Server server) throws IOException {
		registry = LocateRegistry.createRegistry(rmiRegistryPort);
		exportedServer = new RemoteServerImpl(server);
		UnicastRemoteObject.exportObject(exportedServer, serverPort);
		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			registry.rebind(SERVER_NAME, exportedServer);
		}
		finally {
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}
		logger.info("Server advertised via rmi on port " + rmiRegistryPort);
		logger.info("Server exported via rmi on port " + serverPort);
	}
	
	@Override
	public void dispose() {
		try {
			UnicastRemoteObject.unexportObject(exportedServer, true);
		} catch (NoSuchObjectException e) {
			logger.log(Level.WARNING, "Why couldn't I shutdown the server?", e);
		}	
		try {
			UnicastRemoteObject.unexportObject(registry, true);
		} catch (NoSuchObjectException e) {
			logger.log(Level.WARNING, "Why couldn't I close the registry?", e);
		}
	}

}
