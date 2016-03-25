package org.protege.owl.server.experiments;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.api.ChangeHistory;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.policy.RMILoginUtility;
import org.semanticweb.owlapi.model.IRI;

public class TestEmptyDocument {

    public static void main(String[] args) throws RemoteException, NotBoundException, OWLServerException {
        IRI serverLocation = IRI.create(RMIClient.SCHEME + "://localhost:4875");
        AuthToken tok = RMILoginUtility.login(serverLocation, "redmond", "troglodyte");
        RMIClient client = new RMIClient(tok, serverLocation);
        client.initialise();
        client.createRemoteOntology(IRI.create(RMIClient.SCHEME + "://localhost:4875/empty" + ChangeHistory.CHANGE_DOCUMENT_EXTENSION));
    }
}
