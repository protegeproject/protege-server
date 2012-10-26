package org.protege.owl.server.connect.rmi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.ClientFactory;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.AuthenticationFailedException;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.DocumentFactoryImpl;
import org.protege.owl.server.connect.RootUtils;
import org.protege.owl.server.policy.RMILoginUtility;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class AbstractRMIClientFactory implements ClientFactory {
    private Logger logger = Logger.getLogger(AbstractRMIClientFactory.class.getCanonicalName());
    private Map<IRI, AuthToken> authMap = new TreeMap<IRI, AuthToken>();
    private DocumentFactory factory = new DocumentFactoryImpl();
    
    @Override
    public boolean hasSuitableMetaData(OWLOntology ontology) throws IOException {
        if (factory.hasServerMetadata(ontology)) {
            return isSuitable(factory.getServerLocation(ontology));
        }
        return false;
    }
    
    @Override
    public VersionedOntologyDocument getVersionedOntologyDocument(OWLOntology ontology) throws IOException {
        return factory.getVersionedOntologyDocument(ontology);
    }
    
   @Override
   public RMIClient connectToServer(OWLOntology ontology) throws OWLServerException, IOException {
        return connectToServer(factory.getServerLocation(ontology));
    }

    @Override
    public boolean isSuitable(IRI serverLocation) {
        return serverLocation.getScheme().equals(RMIClient.SCHEME);
    }

    @Override
    public RMIClient connectToServer(IRI serverLocation) throws OWLServerException {
        try {
            IRI serverRoot = RootUtils.getRoot(serverLocation);
            AuthToken authToken = authMap.get(serverRoot);
            if (authToken != null && !RMILoginUtility.verify(serverLocation, authToken)) {
                authMap.remove(serverRoot);
                authToken = null;
            }
            if (authToken == null) {
                authToken = login(serverLocation);
            }
            RMIClient client = new RMIClient(authToken, serverLocation);
            client.initialise();
            authMap.put(serverRoot, authToken);
            return client;
        }
        catch (NotBoundException nbe) {
            throw new OWLServerException(nbe);
        }
        catch (RemoteException re) {
            throw new OWLServerException(re);
        }
        catch (URISyntaxException use) {
            throw new OWLServerException(use);
        }
    }
    
    @Override
    public Client connectToServer(IRI serverLocation, Properties info) throws OWLServerException {
        String username = info.getProperty(ClientFactory.USERNAME_KEY);
        String password = info.getProperty(ClientFactory.PASSWORD_KEY);
        if (username == null || password == null) {
            throw new AuthenticationFailedException("No credentials supplied.");
        }
        AuthToken authToken;
        try {
            authToken = login(serverLocation, username, password);
        }
        catch (NotBoundException e) {
            throw new AuthenticationFailedException("Internal failure processing authentication credentials", e);
        }
        catch (RemoteException e) {
            throw new AuthenticationFailedException("Internal failure processing authentication credentials", e);
        }
        RMIClient client = new RMIClient(authToken, serverLocation);
        try {
            client.initialise();
        }
        catch (RemoteException re) {
            throw new OWLServerException(re);
        }
        catch (NotBoundException nbe) {
            throw new OWLServerException(nbe);
        }
        authMap.put(serverLocation, authToken);
        return client;
    }
    
    /**
     * Obtain an authentication token.  Does not return null.
     * <p/>
     * This method allows subtypes to customize how authentication is performed.  Subtypes can use wired in authentication
     * (as is done by some unit tests), use a swing dialog to ask for credentials from the user, or get credentials from the 
     * command line.
     * 
     * @param serverLocation
     * @return
     * @throws AuthenticationFailedException
     */
    protected abstract AuthToken login(IRI serverLocation) throws AuthenticationFailedException;
    
    protected AuthToken login(IRI serverLocation, String username, String password) throws RemoteException, NotBoundException {
        return RMILoginUtility.login(serverLocation, username, password);
    }
    
    @Override
    public boolean hasReadyConnection(IRI serverLocation) {
        try {
            IRI rootLocation = RootUtils.getRoot(serverLocation);
            return authMap.containsKey(rootLocation);
        }
        catch (URISyntaxException e) {
            logger.log(Level.WARNING, "IRI has invalid format: " + serverLocation, e);
            return false;
        }
    }
    
    @Override
    public Set<IRI> getReadyConnections() {
        return authMap.keySet();
    }
    
    @Override
    public RMIClient quickConnectToServer(IRI serverLocation) {
        IRI serverRoot = null;
        try {
            serverRoot = RootUtils.getRoot(serverLocation);
            AuthToken authToken = authMap.get(serverRoot);
            if (authToken == null) {
                return null;
            }
            else if (!RMILoginUtility.verify(serverLocation, authToken)) {
                authMap.remove(serverRoot);
                return null;
            }
            RMIClient client = new RMIClient(authToken, serverLocation);
            client.initialise();
            return client;
        }
        catch (NotBoundException nbe) {
            logger.warning("Server connection to " + serverLocation + " lost.");
            authMap.remove(serverRoot);
            return null;
        }
        catch (RemoteException re) {
            logger.warning("Server connection to " + serverLocation + " lost.");
            authMap.remove(serverRoot);
            return null;
        }
        catch (URISyntaxException use) {
            logger.log(Level.WARNING, "IRI has invalid format: " + serverLocation, use);
            return null;
        }
    }

}
