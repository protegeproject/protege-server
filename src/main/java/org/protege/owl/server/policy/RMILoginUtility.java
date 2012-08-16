package org.protege.owl.server.policy;

import java.net.URI;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.protege.owl.server.api.User;
import org.semanticweb.owlapi.model.IRI;

public class RMILoginUtility {
    
    private RMILoginUtility() {
        ;
    }
    
    public static User login(IRI serverlocation, String username, String password) throws RemoteException, NotBoundException {
        URI uri = serverlocation.toURI();
        return login(uri.getHost(), uri.getPort(), username, password);
    }
    
    public static User login(String host, int port, String username, String password) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", port);
        LoginService server = (LoginService) registry.lookup(LoginService.SERVICE);
        return server.login(username, password);
    }
}
