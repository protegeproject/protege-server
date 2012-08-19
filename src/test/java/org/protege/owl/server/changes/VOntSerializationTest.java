package org.protege.owl.server.changes;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;

import junit.framework.Assert;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.TestUtilities;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.policy.UnauthorizedToken;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class VOntSerializationTest {
    private AuthToken authToken = new UnauthorizedToken("redmond");
    private IRI serverLocation; 
    private Framework framework;

    @Parameters({ "rmiPort" })
    @BeforeClass
    public void getServerPort(int rmiPort) {
        serverLocation = IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/Pizza.history");
    }
    
    @BeforeMethod
    public void startAndSeedServer() throws Exception {
        TestUtilities.initializeServerRoot();
        framework = TestUtilities.startServer("server-basic-config.xml", "metaproject-001.owl");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("src/test/resources/pizza.owl"));
        RMIClient client = new RMIClient(authToken, serverLocation);
        client.initialise();
        new ClientUtilities(client).createServerOntology(serverLocation, new ChangeMetaData("Seeded Pizza"), ontology);
    }

    @AfterMethod
    public void stopServer() throws OWLServerException {
        try {
            framework.stop();
            framework.waitForStop(60 * 60 * 1000);
        }
        catch (InterruptedException ie) {
            throw new OWLServerException(ie);
        }
        catch (BundleException be) {
            throw new OWLServerException(be);
        }
    }
    
    @Test
    public void serializationTest() throws OWLOntologyCreationException, OWLServerException, IOException, OWLOntologyStorageException, NotBoundException {
        File ontologyFile = write();
        read(ontologyFile);
    }
    
    private File write() throws OWLServerException, OWLOntologyCreationException, IOException, OWLOntologyStorageException, NotBoundException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        RMIClient client = new RMIClient(authToken, serverLocation);
        client.initialise();
        RemoteOntologyDocument doc = (RemoteOntologyDocument) client.getServerDocument(serverLocation);
        VersionedOntologyDocument vont = new ClientUtilities(client).loadOntology(manager, doc);
        File ontologyFile = File.createTempFile("Pizza", ".owl");
        manager.saveOntology(vont.getOntology(), IRI.create(ontologyFile));
        manager.setOntologyDocumentIRI(vont.getOntology(), IRI.create(ontologyFile));
        boolean saveSucceeded = vont.saveMetaData();
        Assert.assertTrue(saveSucceeded);
        Assert.assertTrue(VersionedOntologyDocumentImpl.getHistoryFile(ontologyFile).exists());
        return ontologyFile;
    }
    
    private void read(File ontologyFile) throws OWLOntologyCreationException, IOException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        DocumentFactory factory = new DocumentFactoryImpl();
        Assert.assertTrue(factory.hasServerMetadata(ontology));
        VersionedOntologyDocument vont = factory.getVersionedOntologyDocument(ontology);
        Assert.assertNotNull(vont);
        Assert.assertEquals(new OntologyDocumentRevision(1), vont.getRevision());
        Assert.assertEquals(serverLocation, vont.getServerDocument().getServerLocation());
    }
}
