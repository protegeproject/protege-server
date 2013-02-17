package org.protege.owl.server;

import static org.protege.owl.server.TestUtilities.REDMOND;
import static org.protege.owl.server.TestUtilities.VENDETTI;
import static org.protege.owl.server.TestUtilities.PASSWORD_MAP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.antlr.runtime.RecognitionException;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerTransport;
import org.protege.owl.server.connect.local.LocalClient;
import org.protege.owl.server.connect.local.LocalTransportImpl;
import org.protege.owl.server.core.ServerImpl;
import org.protege.owl.server.policy.Authenticator;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AuthenticatedTest {
    public static final IRI SERVER_TEST_ONT = IRI.create(LocalClient.SCHEME + "://localhost/Test" + ChangeHistory.CHANGE_DOCUMENT_EXTENSION);
    private Server server;
    private LocalTransportImpl transport;
    
    private Client client1, client2;
    private VersionedOntologyDocument vont1;
    

    @BeforeMethod
    public void startServer() throws IOException, RecognitionException, OWLServerException {
        TestUtilities.initializeServerRoot();
        Server coreServer =  new ServerImpl(TestUtilities.ROOT_DIRECTORY, TestUtilities.CONFIGURATION_DIRECTORY);
        server = new Authenticator(coreServer);
        transport = new LocalTransportImpl();
        List<ServerTransport> transports = new ArrayList<ServerTransport>();
        transports.add(transport);
        transport.start(server);
        server.setTransports(transports);
    }
    
    @AfterMethod
    public void shutdownServer() {
        server.shutdown();
    }
    
    @Test
    public void testCommitConflict() throws OWLOntologyCreationException, OWLServerException {
        setupClient1();
        setupClient2();
        RemoteOntologyDocument testDoc = vont1.getServerDocument();
        OWLOntology ontology1 = vont1.getOntology();
        Assert.assertFalse(ontology1.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        OntologyDocumentRevision revisionBeforeCommits = vont1.getRevision();
        TestUtilities.rawCommit(client1, testDoc, revisionBeforeCommits, new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        TestUtilities.rawCommit(client2, testDoc, revisionBeforeCommits, new RemoveAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        
        ChangeHistory fullHistory = client1.getChanges(testDoc, OntologyDocumentRevision.START_REVISION.asPointer(), RevisionPointer.HEAD_REVISION);
        
        Assert.assertEquals(revisionBeforeCommits.add(1), fullHistory.getEndRevision());
        List<OWLOntologyChange> firstChanges = fullHistory.cropChanges(revisionBeforeCommits, revisionBeforeCommits.next()).getChanges(ontology1);
        Assert.assertEquals(1, firstChanges.size());
        Assert.assertEquals(new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION), firstChanges.get(0));
    }
    
    /**
     * Test self conflict.
     * <p/>
     * Originally I had thought that a user should not have conflicts with himself.  But this led to problems with the getUncommittedChanges
     * and reverse updates (e.g., a reverse update by a user past commits from the same user will lead to a report of uncommitted changes).
     * The current strategy simplifies the definition of the server side commit at the expense of some additional complexity in complicated 
     * clients that do several commits in sequence.
     * 
     * @throws OWLOntologyCreationException
     * @throws OWLServerException
     */
    @Test
    public void testSelfConflict() throws OWLOntologyCreationException, OWLServerException {
        setupClient1();
        RemoteOntologyDocument testDoc = vont1.getServerDocument();
        OWLOntology ontology1 = vont1.getOntology();
        Assert.assertFalse(ontology1.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        OntologyDocumentRevision revisionBeforeCommits = vont1.getRevision();
        TestUtilities.rawCommit(client1, testDoc, revisionBeforeCommits, new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        TestUtilities.rawCommit(client1, testDoc, revisionBeforeCommits, new RemoveAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        
        ChangeHistory fullHistory = client1.getChanges(testDoc, OntologyDocumentRevision.START_REVISION.asPointer(), RevisionPointer.HEAD_REVISION);
        
        Assert.assertEquals(revisionBeforeCommits.add(1), fullHistory.getEndRevision());
        List<OWLOntologyChange> firstChanges = fullHistory.cropChanges(revisionBeforeCommits, revisionBeforeCommits.next()).getChanges(ontology1);
        Assert.assertEquals(1, firstChanges.size());
        Assert.assertEquals(new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION), firstChanges.get(0));
    }
    
    @Test
    public void testUserIdAdded() throws OWLOntologyCreationException, OWLServerException {
        setupClient1();
        OntologyDocumentRevision revisionBeforeCommit = vont1.getRevision();
        RemoteOntologyDocument testDoc = vont1.getServerDocument();
        OWLOntology ontology1 = vont1.getOntology();
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        changes.add(new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        client1.commit(testDoc, client1.getDocumentFactory().createChangeDocument(changes, new ChangeMetaData(), vont1.getRevision()));
        ChangeHistory committedChange = client1.getChanges(testDoc, revisionBeforeCommit.asPointer(), revisionBeforeCommit.next().asPointer());
        Assert.assertEquals(1, committedChange.getChanges(ontology1).size());
        Assert.assertEquals(client1.getUserId(), committedChange.getMetaData(revisionBeforeCommit).getUserId());
    }
    
    /**
     * Test that a commit implies an update.
     * <p/>
     * This could change in a future version of this server but changing it would add some complexity to the server.
     * If this changes the VersionedOntologyDocument would need to include some information about what changes have already 
     * been committed.  Things to think about if we change the commit implying update are the following cases
     * <ol>
     * <li>a user replaces axiom1 with axiom2, commits and then replaces axiom 2 with axiom3 and commits</li/>
     * <li>the user performs the above changes and then update to the head and then updates back two revisions.  How 
     *     should his state be different after updating back than it was when making the changes?  What should happen if
     *     he then replaces axiom1 with axiom4?
     * <li> the user performs the steps in step 1, updates to head, replaces axiom3 with axiom 4 but does not commit and then 
     *      reverts two revisions back.
     * </ol>
     * 
     * 
     * @throws OWLOntologyCreationException
     * @throws OWLServerException
     */
    @Test
    public void testCommitImpliesUpdate() throws OWLOntologyCreationException, OWLServerException {
    	setupClient1();
    	setupClient2();
    	RemoteOntologyDocument testDoc = vont1.getServerDocument();
    	OWLOntology ontology1 = vont1.getOntology();
    	
    	Assert.assertFalse(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION.equals(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
    	Assert.assertFalse(ontology1.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
    	Assert.assertFalse(ontology1.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
    	Assert.assertEquals(OntologyDocumentRevision.START_REVISION, vont1.getRevision());
    	Assert.assertEquals(OntologyDocumentRevision.START_REVISION, client1.evaluateRevisionPointer(testDoc, RevisionPointer.HEAD_REVISION));
    	
    	TestUtilities.rawCommit(client2, testDoc, vont1.getRevision(), new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
    	Assert.assertEquals(OntologyDocumentRevision.START_REVISION.add(1), client1.evaluateRevisionPointer(testDoc, RevisionPointer.HEAD_REVISION));
    	
    	List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
    	changes.add(new AddAxiom(ontology1, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
    	ontology1.getOWLOntologyManager().applyChanges(changes);
    	ClientUtilities.commit(client1, new ChangeMetaData("commit after client2's commit"), vont1);
    	Assert.assertEquals(OntologyDocumentRevision.START_REVISION.add(2), client1.evaluateRevisionPointer(testDoc, RevisionPointer.HEAD_REVISION));
    	Assert.assertTrue(ontology1.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
    	Assert.assertTrue(ontology1.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
    }
    
    
    
    
    private void setupClient1() throws OWLOntologyCreationException, OWLServerException {
        client1 = getClient(REDMOND.getUserName(), PASSWORD_MAP.get(REDMOND));
        OWLOntology ontology1 = OWLManager.createOWLOntologyManager().createOntology();
        vont1 = ClientUtilities.createAndGetServerOntology(client1, SERVER_TEST_ONT, new ChangeMetaData(), ontology1);
    }
    
    private void setupClient2() throws  OWLOntologyCreationException, OWLServerException {
        client2 = getClient(VENDETTI.getUserName(), PASSWORD_MAP.get(VENDETTI));
    }
    
    private LocalClient getClient(String username, String password) {
        AuthToken token = Authenticator.localLogin(transport, username, password);
        return transport.getClient(token);
    }

}
