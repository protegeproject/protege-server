package org.protege.owl.server.policy;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.protege.owl.server.api.AuthToken;
import org.semanticweb.owlapi.model.IRI;

public class RMILoginUtility {
    
    private RMILoginUtility() {
        ;
    }
    
    public static AuthToken login(IRI serverlocation, String username, String password) throws RemoteException, NotBoundException {
        URI uri = serverlocation.toURI();
        return login(uri.getHost(), uri.getPort(), username, password);
    }
    
    public static AuthToken login(String host, int port, String username, String password) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        LoginService server = (LoginService) registry.lookup(LoginService.SERVICE);
        return server.login(username, password);
    }
    
    public static boolean verify(IRI serverLocation, AuthToken token) throws RemoteException, NotBoundException {
        URI uri = serverLocation.toURI();
        return verify(uri.getHost(), uri.getPort(), token);
    }
    
    public static boolean verify(String host, int port, AuthToken token) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host, port);
        LoginService server = (LoginService) registry.lookup(LoginService.SERVICE);
        return server.checkAuthentication(token);
    }
}
