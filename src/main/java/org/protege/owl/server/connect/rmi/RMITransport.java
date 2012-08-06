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

public class RMITransport implements ServerTransport {
	public static final String SERVER_NAME = "OWL 2 Server";
	
	private Logger logger = Logger.getLogger(RMITransport.class.getCanonicalName());
	RemoteServer exportedServer;
	private Registry registry;
	private int port;
	
	public RMITransport(int port) {
		this.port = port;
	}

	@Override
	public void start(Server server) throws IOException {
		registry = LocateRegistry.createRegistry(port);
		exportedServer = (new RemoteServerImpl(server));
		UnicastRemoteObject.exportObject(exportedServer);
		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			registry.rebind(SERVER_NAME, exportedServer);
		}
		finally {
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}
		logger.info("Server exported via rmi on port " + port);
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
