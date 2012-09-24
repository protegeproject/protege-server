package org.protege.owl.server.experiments;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.RemoteServerDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.policy.RMILoginUtility;
import org.semanticweb.owlapi.model.IRI;

public class Test {
    public static void main(String[] args) throws RemoteException, NotBoundException, OWLServerException {
        String host = "171.65.32.14";
        int rmiPort = 4875;
        AuthToken tim = RMILoginUtility.login(host, rmiPort, "redmond", "troglodyte");
        RMIClient client = new RMIClient(tim, host, rmiPort);
        client.initialise();
        IRI serverIRI = IRI.create(RMIClient.SCHEME + "://" + host + ":" + rmiPort);
        System.out.println(serverIRI);
        RemoteServerDirectory dir = (RemoteServerDirectory) client.getServerDocument(serverIRI);
        for (RemoteServerDocument doc : client.list(dir)) {
            System.out.println("found: " + doc.getServerLocation());
        }
     }
}
