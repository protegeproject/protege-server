package org.protege.owl.server.policy;

import static org.protege.owl.server.PizzaVocabulary.CHEESEY_PIZZA_DEFINITION;
import static org.protege.owl.server.TestUtilities.FERGERSON;
import static org.protege.owl.server.TestUtilities.GUEST;
import static org.protege.owl.server.TestUtilities.REDMOND;

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
import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.exception.AuthorizationFailedException;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
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
    private IRI fergersonsPizzaLoc;
    private IRI redmondsPrivateDirLoc;
    private IRI redmondsPrivatePizzaLoc;

    private Framework framework;
    private int rmiPort;
    
    @BeforeClass
    @Parameters({ "rmiPort" })
    public void setRMIPort(int rmiPort) {
        this.rmiPort = rmiPort;
        fergersonsPizzaLoc = IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/pizza-fergerson.history");
        String redmondsPrivatePrefix = RMIClient.SCHEME + "://localhost:" + rmiPort + "/redmonds-private-directory";
        redmondsPrivateDirLoc = IRI.create(redmondsPrivatePrefix);
        redmondsPrivatePizzaLoc = IRI.create(redmondsPrivatePrefix + "/pizza.history");
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
    public void testCantTraverseParentDir() throws OWLOntologyCreationException, NotBoundException, OWLServerException, RemoteException {
        createRedmondsPrivateDir();
        Client redmondsClient = TestUtilities.createClient(rmiPort, REDMOND);
        RemoteOntologyDocument pizzaDoc = (RemoteOntologyDocument) redmondsClient.getServerDocument(redmondsPrivatePizzaLoc);
        Client guestClient = TestUtilities.createClient(rmiPort, GUEST);
        boolean authorizationFailed = false;
        try {
             guestClient.evaluateRevisionPointer(pizzaDoc, RevisionPointer.HEAD_REVISION);
        }
        catch (AuthorizationFailedException afe) {
            authorizationFailed = true;
        }
        Assert.assertTrue(authorizationFailed);
    }
    
    @Test
    public void testEvaluatePointerFailure() throws RemoteException, OWLOntologyCreationException, NotBoundException, OWLServerException {
        createFergersonsPizza();
        // use fergersons client to get the remote ontology document in case a future version of the policy prevents guest from getting it.
        Client fergersonsClient = TestUtilities.createClient(rmiPort, FERGERSON);
        RemoteOntologyDocument pizzaDoc = (RemoteOntologyDocument) fergersonsClient.getServerDocument(fergersonsPizzaLoc);
        Client guestClient = TestUtilities.createClient(rmiPort, GUEST);
        boolean authorizationFailed = false;
        try {
             guestClient.evaluateRevisionPointer(pizzaDoc, RevisionPointer.HEAD_REVISION);
        }
        catch (AuthorizationFailedException afe) {
            authorizationFailed = true;
        }
        Assert.assertTrue(authorizationFailed);
    }
    
    @Test
    public void testListFailure() throws RemoteException, OWLOntologyCreationException, NotBoundException, OWLServerException {
        createRedmondsPrivateDir();
        Client redmondsClient = TestUtilities.createClient(rmiPort, REDMOND);
        RemoteServerDirectory redmondsDir = (RemoteServerDirectory) redmondsClient.getServerDocument(redmondsPrivateDirLoc);
        Client guestClient = TestUtilities.createClient(rmiPort, GUEST);
        boolean authorizationFailed = false;
        try {
             guestClient.list(redmondsDir);
        }
        catch (AuthorizationFailedException afe) {
            authorizationFailed = true;
        }
        Assert.assertTrue(authorizationFailed);
    }
    
    @Test
    public void testCommitFailure() throws RemoteException, NotBoundException, OWLOntologyCreationException, OWLServerException {
        OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
        createFergersonsPizza();
        Client guestClient = TestUtilities.createClient(rmiPort, GUEST);
        RemoteOntologyDocument remotePizza = (RemoteOntologyDocument) guestClient.getServerDocument(fergersonsPizzaLoc);
        boolean authorizationFailed = false;
        try {
            TestUtilities.rawCommit(guestClient, remotePizza, new OntologyDocumentRevision(1), new RemoveAxiom(ontology, CHEESEY_PIZZA_DEFINITION));
        }
        catch (AuthorizationFailedException afe) {
            authorizationFailed = true;
        }
        Assert.assertTrue(authorizationFailed);
    }
    
    private void createFergersonsPizza() throws RemoteException, NotBoundException, OWLOntologyCreationException, OWLServerException {
        Client fergersonsClient = TestUtilities.createClient(rmiPort, FERGERSON);
        OWLOntology pizza = PizzaVocabulary.loadPizza();
        ClientUtilities.createServerOntology(fergersonsClient, fergersonsPizzaLoc, new ChangeMetaData(), pizza);
    }
    
    private void createRedmondsPrivateDir() throws RemoteException, NotBoundException, OWLServerException, OWLOntologyCreationException {
        Client redmondsClient = TestUtilities.createClient(rmiPort, REDMOND);
        redmondsClient.createRemoteDirectory(redmondsPrivateDirLoc);
        OWLOntology pizza = PizzaVocabulary.loadPizza();
        ClientUtilities.createServerOntology(redmondsClient, redmondsPrivatePizzaLoc, new ChangeMetaData(), pizza);
    }
}
