package org.protege.owl.server.core;

import static org.protege.owl.server.TestUtilities.REDMOND;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import junit.framework.Assert;

import org.protege.owl.server.PizzaVocabulary;
import org.protege.owl.server.TestUtilities;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerTransport;
import org.protege.owl.server.changes.VersionedOntologyDocumentImpl;
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
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the robustness of the server when the power is turned off (no chance for recovery) while the server is saving a file.
 * @author redmond
 *
 */
public class RobustnessTest {
    public static String TEST_FILE = "test.history";
    public static final IRI TEST_SERVER_IRI = IRI.create(LocalClient.SCHEME + "://localhost/" + TEST_FILE);
    private Server server;
    private LocalTransport transport;

    @AfterMethod
    @BeforeMethod
    public void initializeRoot() throws IOException {
        TestUtilities.initializeServerRoot();
    }
    
    private void startServer() throws IOException {
        server = new ServerImpl(TestUtilities.ROOT_DIRECTORY, TestUtilities.CONFIGURATION_DIRECTORY);
        transport = new LocalTransportImpl();
        server.setTransports(Collections.singleton((ServerTransport) transport));
        transport.start(server);
    }
    
    public void stopServer() {
        server.shutdown();
    }
    
    private Client createClient() {
        AuthToken tok = new UnauthorizedToken(REDMOND.getUserName());
        return transport.getClient(tok);
    }
    
    @Test
    public void corruptedBaseline() throws IOException, OWLServerException, OWLOntologyCreationException {
        corruptTestHistory();
        startServer();
        try {
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
        finally {
            stopServer();
        }
    }
    
    @Test
    public void corruptedSaveTest() throws OWLOntologyCreationException, IOException, OWLServerException {
        twoForcedSaves();

        /* this simulates pulling the plug while the save was in progress */
        corruptTestHistory();

        startServer();
        try {
            Client client = createClient();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            RemoteOntologyDocument doc = (RemoteOntologyDocument) client.getServerDocument(TEST_SERVER_IRI);
            VersionedOntologyDocument vont = ClientUtilities.loadOntology(client, manager, doc);
            Assert.assertEquals(new OntologyDocumentRevision(1), vont.getRevision());
            Assert.assertTrue(vont.getOntology().containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
            Assert.assertFalse(vont.getOntology().containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        }
        finally {
            stopServer();
        }
    }

    /**
     * Test that the state of a server can be recovered from a client history.
     * <p/>
     * This is a bit of a strange test because it is testing an implementation detail that people might want to change
     * at a later time.  But it does add an element of robustness to the server to know that the server state is also saved on the clients 
     * that are up to date.  It is documented on the wiki and if this test is removed then the wiki should be changed in a corresponding way.
     * Future versions of this test might require some additional steps to ensure that the client is fully up to date with the server copy.
     * 
     * @throws OWLOntologyCreationException
     * @throws IOException
     * @throws OWLServerException
     * @throws OWLOntologyStorageException
     */
    @Test
    public void restoreFromClient() throws OWLOntologyCreationException, IOException, OWLServerException, OWLOntologyStorageException {
        File clientCopy = TestUtilities.createFileInTempDirectory("CorruptPizza.owl");
        
        twoForcedSaves();
        
        // client1 is up to date and has a full copy of the server data.
        startServer();
        try {
            Client client1 = createClient();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            RemoteOntologyDocument doc = (RemoteOntologyDocument) client1.getServerDocument(TEST_SERVER_IRI);
            VersionedOntologyDocument vont = ClientUtilities.loadOntology(client1, manager, doc);
            manager.setOntologyDocumentIRI(vont.getOntology(), IRI.create(clientCopy));
            manager.saveOntology(vont.getOntology());
            vont.saveMetaData();
        }
        finally {
            stopServer();
        }
        
        // the server state is wiped and then recovered from a client.
        TestUtilities.initializeServerRoot();
        File clientHistory = VersionedOntologyDocumentImpl.getHistoryFile(clientCopy);
        copy(clientHistory, new File(TestUtilities.ROOT_DIRECTORY, TEST_FILE));
        
        // the server is fully restored.
        startServer();
        try {
            Client client2 = createClient();
            OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
            RemoteOntologyDocument doc = (RemoteOntologyDocument) client2.getServerDocument(TEST_SERVER_IRI);
            VersionedOntologyDocument vont2 = ClientUtilities.loadOntology(client2, manager2, doc);
            Assert.assertEquals(new OntologyDocumentRevision(2), vont2.getRevision());
            Assert.assertTrue(vont2.getOntology().containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
            Assert.assertTrue(vont2.getOntology().containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        }
        finally {
            stopServer();
        }
    }
    
    /**
     * This call will put the test history on the server in a known state.
     * <p/>
     * The test history contains three revisions 0,1,2. The delta from 0->1 contains one 
     * added axiom and the delta from 1->2 contains another.  The backup file only contains the 
     * revisions 0 and 1.
     * 
     * @throws IOException
     * @throws OWLServerException
     * @throws OWLOntologyCreationException
     */
    private void twoForcedSaves() throws IOException, OWLServerException, OWLOntologyCreationException {
        OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
        RemoteOntologyDocument testDoc;
        startServer();
        try {
            Client client0 = createClient();
            testDoc = client0.createRemoteOntology(TEST_SERVER_IRI);
            TestUtilities.rawCommit(client0, testDoc, OntologyDocumentRevision.START_REVISION, new AddAxiom(ontology, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        }
        finally {
            stopServer();
        }
        startServer();
        try {
            Client client1 = createClient();
            TestUtilities.rawCommit(client1, testDoc, OntologyDocumentRevision.START_REVISION.next(), new AddAxiom(ontology, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        }
        finally {
            stopServer();
        }
    }
    
    private void corruptTestHistory() throws IOException {
        File toCorrupt = new File(TestUtilities.ROOT_DIRECTORY, TEST_FILE);
        FileWriter writer = new FileWriter(toCorrupt);
        writer.write("hello world");
        writer.flush();
        writer.close();
    }
    
    private void copy(File input, File output) throws IOException {
        InputStream is = new FileInputStream(input);
        OutputStream out = new FileOutputStream(output);
        try {
            int c;
            while ((c = is.read()) >= 0) {
                out.write((byte) c);
            }
        }
        finally {
            is.close();
            out.flush();
            out.close();
        }
    }
    
}
