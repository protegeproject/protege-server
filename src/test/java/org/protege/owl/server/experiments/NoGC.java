package org.protege.owl.server.experiments;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.policy.RMILoginUtility;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class NoGC {
    
    public static void main(String[] args) throws RemoteException, NotBoundException, OWLServerException, OWLOntologyCreationException {
        int rmiPort = 5100;
        AuthToken tim = RMILoginUtility.login("localhost", rmiPort, "redmond", "troglodyte");
        while (true) {
            try {
                RMIClient client = new RMIClient(tim,"localhost", rmiPort);
                client.initialise();
                RemoteOntologyDocument pizzaDoc = (RemoteOntologyDocument) client.getServerDocument(IRI.create(RMIClient.SCHEME + "://localhost:5100/Pizza.history"));
                ChangeHistory changes = client.getChanges(pizzaDoc, OntologyDocumentRevision.START_REVISION.asPointer(), RevisionPointer.HEAD_REVISION);
                if (changes.getChanges(OWLManager.createOWLOntologyManager().createOntology()).size() != 945) {
                    System.out.println("Hmm");
                    System.exit(-1);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
