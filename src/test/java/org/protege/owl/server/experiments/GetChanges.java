package org.protege.owl.server.experiments;

import static org.protege.owl.server.TestUtilities.PASSWORD_MAP;
import static org.protege.owl.server.TestUtilities.REDMOND;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.api.ChangeHistory;
import org.protege.owl.server.changes.api.RemoteOntologyDocument;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.policy.RMILoginUtility;
import org.semanticweb.owlapi.model.IRI;

public class GetChanges {
    private static final IRI SERVER_LOCATION = IRI.create("rmi-owl2-server://tania12g:4875/Pizza.history");

    public static void main(String[] args) throws RemoteException, NotBoundException, OWLServerException {
        AuthToken auth = RMILoginUtility.login(SERVER_LOCATION , REDMOND.getUserName(), PASSWORD_MAP.get(REDMOND));
        RMIClient client = new RMIClient(auth, SERVER_LOCATION);
        client.initialise();
        RemoteOntologyDocument doc = (RemoteOntologyDocument) client.getServerDocument(SERVER_LOCATION);
        ChangeHistory history = client.getChanges(doc, new OntologyDocumentRevision(2).asPointer(), new OntologyDocumentRevision(3).asPointer());
        System.out.println("history = " + history);
    }
}
