package org.protege.owl.server.experiments;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.client.RemoteServerDirectory;
import org.protege.owl.server.api.client.RemoteServerDocument;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.policy.RMILoginUtility;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class Test {
    public static void main(String[] args) throws Exception {
        test02();
     }
    
    public static void test01() throws RemoteException, NotBoundException, OWLServerException {
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
    
    public static void test02() throws RemoteException, NotBoundException, OWLServerException, OWLOntologyCreationException {
        String host = "171.65.32.14";
        int rmiPort = 4875;
        AuthToken tim = RMILoginUtility.login(host, rmiPort, "redmond", "troglodyte");
        RMIClient client = new RMIClient(tim, host, rmiPort);
        client.initialise();
        RemoteOntologyDocument doc = (RemoteOntologyDocument) client.getServerDocument(IRI.create(RMIClient.SCHEME + "://" + host + ":" + rmiPort + "/Pizza.history"));
        VersionedOntologyDocument vont = ClientUtilities.loadOntology(client, OWLManager.createOWLOntologyManager(), doc);
        OWLOntology ontology = vont.getOntology();
        System.out.println("Axiom count = " + ontology.getAxiomCount());
    }
}
