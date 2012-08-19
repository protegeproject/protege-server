package org.protege.owl.server.connect.rmi;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.RemoteServerDocument;
import org.protege.owl.server.api.UserId;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.DocumentFactoryImpl;
import org.protege.owl.server.connect.RootUtils;
import org.protege.owl.server.util.AbstractClient;
import org.semanticweb.owlapi.model.IRI;

public class RMIClient extends AbstractClient {
	public static final String SCHEME = "rmi-owl2-server";
	
	private Logger logger = Logger.getLogger(RMIClient.class.getCanonicalName());
	private String host;
	private int port;
	private AuthToken authToken;
	private RemoteServer server;
	
	public RMIClient(AuthToken authenticatedUser, IRI serverLocation) {
	    this.authToken = authenticatedUser;
		URI serverURI = serverLocation.toURI();
		host = serverURI.getHost();
		port = serverURI.getPort();
		if (port < 0) {
			port = Registry.REGISTRY_PORT;
		}
	}
	
	public RMIClient(AuthToken authenticatedUser, String host, int port) {
	    this.authToken = authenticatedUser;
		this.host = host;
		this.port = port;
	}
	
	public void initialise() throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(host, port);
		server = (RemoteServer) registry.lookup(RMITransport.SERVER_NAME);
	}
	
	private OWLServerException processException(RemoteException re) {
	    for (Throwable cause = re.getCause(); cause != null; cause = cause.getCause()) {
	        if (cause instanceof OWLServerException) {
	            return (OWLServerException) cause;
	        }
	    }
	    return new OWLServerException(re);
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
	public UserId getUserId() {
	    return authToken.getUserId();
	}
	
	@Override
	public DocumentFactory getDocumentFactory() {
		return new DocumentFactoryImpl();
	}
	
	@Override
	public RemoteServerDocument getServerDocument(IRI serverIRI) throws OWLServerException {
	    try {
	        return server.getServerDocument(authToken, serverIRI).createRemoteDocument(SCHEME, host, port);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public Collection<RemoteServerDocument> list(RemoteServerDirectory dir)
	        throws OWLServerException {
	    try {
	        return RootUtils.rootList(server.list(authToken, dir.createServerDocument()), SCHEME, host, port);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public RemoteServerDirectory createRemoteDirectory(IRI serverIRI)
	        throws OWLServerException {
	    try {
	        return server.createDirectory(authToken, serverIRI).createRemoteDocument(SCHEME, host, port);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public  RemoteOntologyDocument createRemoteOntology(IRI serverIRI) throws OWLServerException {
	    try {
	        return server.createOntologyDocument(authToken, serverIRI, new TreeMap<String, Object>()).createRemoteDocument(SCHEME, host, port);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public ChangeHistory getChanges(RemoteOntologyDocument doc,
	                                 OntologyDocumentRevision start, OntologyDocumentRevision end)
	                                         throws OWLServerException {
	    try {
	        return server.getChanges(authToken, doc.createServerDocument(), start, end);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}

	@Override
	public void commit(RemoteOntologyDocument doc,
	                    ChangeHistory changes) throws OWLServerException {
	    try {
	        server.commit(authToken, doc.createServerDocument(), changes);
	    }
	    catch (RemoteException re) {
	        throw processException(re);
	    }
	}



}
