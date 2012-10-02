package org.protege.owl.server.policy;

import static org.protege.owl.server.PizzaVocabulary.CHEESEY_PIZZA_DEFINITION;
import static org.protege.owl.server.TestUtilities.FERGERSON;
import static org.protege.owl.server.TestUtilities.GUEST;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.PizzaVocabulary;
import org.protege.owl.server.TestUtilities;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.exception.AuthorizationFailedException;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class PolicyTest {
    private Framework framework;
    private int rmiPort;
    
    @BeforeClass
    @Parameters({ "rmiPort" })
    public void setRMIPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }
    
    @BeforeMethod
    public void startServer() throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, BundleException, InterruptedException {
        TestUtilities.initializeServerRoot();
        framework = TestUtilities.startServer("server-basic-config.xml", "metaproject-003.owl");
    }
    
    @AfterMethod
    public void stopServer() throws BundleException {
        framework.stop();
    }
    
    @Test
    public void testCommitFailure() throws RemoteException, NotBoundException, OWLOntologyCreationException, OWLServerException {
        IRI pizzaLocation = IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/pizza-fergerson.history");
        Client fergersonsClient = TestUtilities.createClient(rmiPort, FERGERSON);
        OWLOntology pizza = PizzaVocabulary.loadPizza();
        ClientUtilities.createServerOntology(fergersonsClient, pizzaLocation, new ChangeMetaData(), pizza);
        Client guestClient = TestUtilities.createClient(rmiPort, GUEST);
        RemoteOntologyDocument remotePizza = (RemoteOntologyDocument) guestClient.getServerDocument(pizzaLocation);
        boolean authorizationFailed = false;
        try {
            TestUtilities.rawCommit(guestClient, remotePizza, new OntologyDocumentRevision(1), new RemoveAxiom(pizza, CHEESEY_PIZZA_DEFINITION));
        }
        catch (AuthorizationFailedException afe) {
            authorizationFailed = true;
        }
        Assert.assertTrue(authorizationFailed);
    }
}
