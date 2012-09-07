package org.protege.owl.server.command;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.policy.RMILoginUtility;
import org.semanticweb.owlapi.model.IRI;

public class Shutdown {

    public static void main(String[] args) throws RemoteException, NotBoundException, OWLServerException {
        String username = args[0];
        String password = args[1];
        IRI serverLocation = IRI.create(RMIClient.SCHEME + "://" + args[2]);
        AuthToken id = RMILoginUtility.login(serverLocation, username, password);
        RMIClient client = new RMIClient(id, serverLocation);
        client.initialise();
        client.shutdown();
    }
}
