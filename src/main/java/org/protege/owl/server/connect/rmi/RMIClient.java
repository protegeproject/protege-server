package org.protege.owl.server.connect.rmi;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.protege.owl.server.impl.DocumentFactoryImpl;
import org.protege.owl.server.util.AbstractClient;
import org.semanticweb.owlapi.model.IRI;

public class RMIClient extends AbstractClient {
	public static final String SCHEME = "rmi-owl2-server";
	
	private Logger logger = Logger.getLogger(RMIClient.class.getCanonicalName());
	private String host;
	private int port;
	private User u = null;
	private RemoteServer server;
	
	public RMIClient(IRI serverLocation) {
		URI serverURI = serverLocation.toURI();
		host = serverURI.getHost();
		port = serverURI.getPort();
		if (port < 0) {
			port = Registry.REGISTRY_PORT;
		}
	}
	
	public RMIClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void initialise() throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(host, port);
		server = (RemoteServer) registry.lookup(RMITransport.SERVER_NAME);
	}
	
	@Override
	public String getScheme() {
		return SCHEME;
	}
	
	@Override
	public String getAuthority() {
		return host;
	}
	
	@Override
	public DocumentFactory getDocumentFactory() {
		return new DocumentFactoryImpl();
	}
	
	@Override
	public ServerDocument getServerDocument(IRI serverIRI) throws RemoteException {
		return server.getServerDocument(u, serverIRI);
	}

	@Override
	public Collection<ServerDocument> list(ServerDirectory dir)
			throws RemoteException {
		return server.list(u, dir);
	}

	@Override
	public ServerDirectory createRemoteDirectory(IRI serverIRI)
			throws RemoteException {
		return server.createDirectory(u, serverIRI);
	}

	@Override
	public RemoteOntologyDocument createRemoteOntology(IRI serverIRI) throws RemoteException {
		return server.createOntologyDocument(u, serverIRI, new TreeMap<String, Object>());
	}

	@Override
	public ChangeDocument getChanges(RemoteOntologyDocument doc,
			OntologyDocumentRevision start, OntologyDocumentRevision end)
			throws RemoteException {
		return server.getChanges(u, doc, start, end);
	}

	@Override
	public void commit(RemoteOntologyDocument doc,
			String commitComment, ChangeDocument changes)
			throws RemoteException {
		server.commit(u, doc, commitComment, changes);
	}

	@Override
	public void shutdown() {
		try {
			server.shutdown();
		}
		catch (RemoteException re) {
			logger.warning("Could not shutdown server");
		}
	}



}
