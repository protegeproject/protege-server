package org.protege.owl.server.core;

import static org.protege.owl.server.TestUtilities.REDMOND;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import junit.framework.Assert;

import org.protege.owl.server.PizzaVocabulary;
import org.protege.owl.server.TestUtilities;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.local.LocalClient;
import org.protege.owl.server.connect.local.LocalTransport;
import org.protege.owl.server.connect.local.LocalTransportImpl;
import org.protege.owl.server.policy.UnauthorizedToken;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RobustnessTest {
    public static String TEST_FILE = "test.history";
    public static final IRI TEST_SERVER_IRI = IRI.create(LocalClient.SCHEME + "://localhost/" + TEST_FILE);
    private Server server;
    private LocalTransport transport;

    @BeforeMethod
    public void startServerOnCleanRoot() throws IOException {
        TestUtilities.initializeServerRoot();
        startServer();
    }
    
    private void startServer() throws IOException {
        server = new ServerImpl(TestUtilities.ROOT_DIRECTORY, TestUtilities.CONFIGURATION_DIRECTORY);
        transport = new LocalTransportImpl();
        server.setTransports(Collections.singleton((ServerTransport) transport));
        transport.start(server);
    }
    
    @AfterMethod
    public void stopServer() {
        server.shutdown();
    }
    
    private void forceSave() throws IOException {
        stopServer();
        startServer();
    }
    

    
    private Client createClient() {
        AuthToken tok = new UnauthorizedToken(REDMOND.getUserName());
        return transport.getClient(tok);
    }
    
    @Test
    public void corruptedBaseline() throws IOException, OWLServerException, OWLOntologyCreationException {
        corruptMainCopy();
        Client client = createClient();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        RemoteOntologyDocument doc = (RemoteOntologyDocument) client.getServerDocument(TEST_SERVER_IRI);
        boolean success = false;
        try {
            ClientUtilities.loadOntology(client, manager, doc);
            success = true;
        }
        catch (RuntimeException e) {
            ;
        }
        catch (OWLServerException e) {
            ;
        }
        Assert.assertFalse(success);
    }
    
    @Test
    public void corruptedSaveTest() throws OWLOntologyCreationException, IOException, OWLServerException {
        twoForcedSaves();
        corruptMainCopy();
        Client client = createClient();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        RemoteOntologyDocument doc = (RemoteOntologyDocument) client.getServerDocument(TEST_SERVER_IRI);
        VersionedOntologyDocument vont = ClientUtilities.loadOntology(client, manager, doc);
        Assert.assertEquals(new OntologyDocumentRevision(1), vont.getRevision());
        Assert.assertTrue(vont.getOntology().containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        Assert.assertFalse(vont.getOntology().containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
    }
    
    private void twoForcedSaves() throws IOException, OWLServerException, OWLOntologyCreationException {
        OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
        Client client0 = createClient();
        RemoteOntologyDocument testDoc = client0.createRemoteOntology(TEST_SERVER_IRI);
        TestUtilities.rawCommit(client0, testDoc, OntologyDocumentRevision.START_REVISION, new AddAxiom(ontology, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        forceSave();
        Client client1 = createClient();
        TestUtilities.rawCommit(client1, testDoc, OntologyDocumentRevision.START_REVISION.next(), new AddAxiom(ontology, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        forceSave();
    }
    
    private void corruptMainCopy() throws IOException {
        File toCorrupt = new File(TestUtilities.ROOT_DIRECTORY, TEST_FILE);
        FileWriter writer = new FileWriter(toCorrupt);
        writer.write("hello world");
        writer.flush();
        writer.close();
    }
    
}
