package org.protege.owl.server.connect.rmi;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.protege.owl.server.api.exception.ServerException;
import org.protege.owl.server.changes.DocumentFactoryImpl;
import org.protege.owl.server.util.AbstractClient;
import org.semanticweb.owlapi.model.IRI;

public class RMIClient extends AbstractClient {
	public static final String SCHEME = "rmi-owl2-server";
	
	private Logger logger = Logger.getLogger(RMIClient.class.getCanonicalName());
	private String host;
	private int port;
	private User user;
	private RemoteServer server;
	
	public RMIClient(User authenticatedUser, IRI serverLocation) {
	    this.user = authenticatedUser;
		URI serverURI = serverLocation.toURI();
		host = serverURI.getHost();
		port = serverURI.getPort();
		if (port < 0) {
			port = Registry.REGISTRY_PORT;
		}
	}
	
	public RMIClient(User authenticatedUser, String host, int port) {
	    this.user = authenticatedUser;
		this.host = host;
		this.port = port;
	}
	
	public void initialise() throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(host, port);
		server = (RemoteServer) registry.lookup(RMITransport.SERVER_NAME);
	}
	
	private ServerException processException(RemoteException re) {
	    for (Throwable cause = re.getCause(); cause != null; cause = cause.getCause()) {
	        if (cause instanceof ServerException) {
	            return (ServerException) cause;
	        }
	    }
	    return new ServerException(re);
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
	public ServerDocument getServerDocument(IRI serverIRI) throws ServerException {
	    try {
	        return server.getServerDocument(user, serverIRI);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public Collection<ServerDocument> list(ServerDirectory dir)
	        throws ServerException {
	    try {
	        return server.list(user, dir);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public ServerDirectory createRemoteDirectory(IRI serverIRI)
	        throws ServerException {
	    try {
	        return server.createDirectory(user, serverIRI);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public RemoteOntologyDocument createRemoteOntology(IRI serverIRI) throws ServerException {
	    try {
	        return server.createOntologyDocument(user, serverIRI, new TreeMap<String, Object>());
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public ChangeDocument getChanges(RemoteOntologyDocument doc,
	                                 OntologyDocumentRevision start, OntologyDocumentRevision end)
	                                         throws ServerException {
	    try {
	        return server.getChanges(user, doc, start, end);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public ChangeDocument commit(RemoteOntologyDocument doc,
	                             ChangeMetaData metaData, ChangeDocument changes, SortedSet<OntologyDocumentRevision> myCommits)
	                                     throws ServerException {
	    try {
	        return server.commit(user, doc, metaData, changes, myCommits);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
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
