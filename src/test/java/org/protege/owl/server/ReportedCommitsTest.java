package org.protege.owl.server;

import static org.protege.owl.server.PizzaVocabulary.CHEESEY_PIZZA_DEFINITION;
import static org.protege.owl.server.PizzaVocabulary.HAS_TOPPING_DOMAIN;
import static org.protege.owl.server.PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION;
import junit.framework.Assert;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.User;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.ChangeDocumentImpl;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.policy.RMILoginUtility;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


/**
 * This test is designed to investigate a bug found while experimenting with the Protege client.  One client reported commits that
 * had been made by the other client.  
 * 
 * The authentication helps indicate bugs in this case though this is not ideal.  Found the bug - was 
 * an off by one error. This will be a useful model for other test cases.
 * 
 * 
 * @author redmond
 *
 */
public class ReportedCommitsTest {
    private Framework framework;
    private int rmiPort;
    
    private User user1;
    private RMIClient client1;
    private ClientUtilities util1;
    private OWLOntologyManager manager1;
    private VersionedOWLOntology vont1;
    
    private User user2;
    private RMIClient client2;
    private ClientUtilities util2;
    private OWLOntologyManager manager2;
    private VersionedOWLOntology vont2;
    
    private RemoteOntologyDocument rontology;
    
    @BeforeClass
    @Parameters({ "rmiPort" })
    public void setRMIPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }
    
    @BeforeMethod
    public void setup() throws Exception {
        TestUtilities.initializeServerRoot();
        framework = TestUtilities.startServer("server-basic-config.xml", "metaproject-002.owl");
        IRI serverLocation = IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/");
        
        user1 = RMILoginUtility.login(serverLocation, "redmond", "troglodyte");
        client1 = new RMIClient(user1, serverLocation);
        client1.initialise();
        util1 = new ClientUtilities(client1);
        
        user2 = RMILoginUtility.login(serverLocation, "vendetti", "jenny");
        client2 = new RMIClient(user2, serverLocation);
        client2.initialise();
        util2 = new ClientUtilities(client2);

        rontology = client1.createRemoteOntology(IRI.create(serverLocation.toString() + "testOntology" + ChangeDocumentImpl.CHANGE_DOCUMENT_EXTENSION));
        manager1 = OWLManager.createOWLOntologyManager();
        vont1 = util1.loadOntology(manager1, rontology);
        manager2 = OWLManager.createOWLOntologyManager();
        vont2 = util2.loadOntology(manager2, rontology);
    }
    
    @AfterMethod
    public void stopServer() throws BundleException {
        framework.stop();
    }
    
    @Test
    public void test01() throws OWLServerException {
        manager1.addAxiom(vont1.getOntology(), HAS_TOPPING_DOMAIN);
        util1.commit(new ChangeMetaData("tim does one"), vont1);
        Assert.assertEquals(1, vont1.getCommittedChanges().size());
        Assert.assertEquals(vont1.getCommittedChanges().get(0).getChanges(vont1.getOntology()).size(), 1);
        Assert.assertEquals(vont1.getCommittedChanges().get(0).getStartRevision(), OntologyDocumentRevision.START_REVISION);
        Assert.assertEquals(vont1.getCommittedChanges().get(0).getEndRevision(), new OntologyDocumentRevision(1));
        
        manager2.addAxiom(vont2.getOntology(), CHEESEY_PIZZA_DEFINITION);
        util2.commit(new ChangeMetaData("jennifer has a shot"), vont2);
        Assert.assertEquals(1, vont2.getCommittedChanges().size());
        Assert.assertEquals(vont2.getCommittedChanges().get(0).getChanges(vont2.getOntology()).size(), 1);
        Assert.assertEquals(vont2.getCommittedChanges().get(0).getStartRevision(), new OntologyDocumentRevision(1));
        Assert.assertEquals(vont2.getCommittedChanges().get(0).getEndRevision(), new OntologyDocumentRevision(2));
        
        manager1.addAxiom(vont1.getOntology(), NOT_CHEESEY_PIZZA_DEFINITION);
        util1.commit(new ChangeMetaData("tim does another one"), vont1);
        Assert.assertEquals(2, vont1.getCommittedChanges().size());
        Assert.assertEquals(vont1.getCommittedChanges().get(0).getChanges(vont1.getOntology()).size(), 1);
        Assert.assertEquals(vont1.getCommittedChanges().get(0).getStartRevision(), OntologyDocumentRevision.START_REVISION);
        Assert.assertEquals(vont1.getCommittedChanges().get(0).getEndRevision(), new OntologyDocumentRevision(1));
        Assert.assertEquals(vont1.getCommittedChanges().get(1).getChanges(vont1.getOntology()).size(), 1);
        Assert.assertEquals(vont1.getCommittedChanges().get(1).getStartRevision(), new OntologyDocumentRevision(2));
        Assert.assertEquals(vont1.getCommittedChanges().get(1).getEndRevision(), new OntologyDocumentRevision(3));
        
        // while we are here we might as well...
        Assert.assertFalse(vont1.getOntology().containsAxiom(CHEESEY_PIZZA_DEFINITION));
        util1.update(vont1);
        Assert.assertTrue(vont1.getOntology().containsAxiom(CHEESEY_PIZZA_DEFINITION));
        
        Assert.assertFalse(vont2.getOntology().containsAxiom(HAS_TOPPING_DOMAIN));
        Assert.assertFalse(vont2.getOntology().containsAxiom(NOT_CHEESEY_PIZZA_DEFINITION));
        util2.update(vont2);
        Assert.assertTrue(vont2.getOntology().containsAxiom(HAS_TOPPING_DOMAIN));
        Assert.assertTrue(vont2.getOntology().containsAxiom(NOT_CHEESEY_PIZZA_DEFINITION));
    }
}
