package org.protege.owl.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.antlr.runtime.RecognitionException;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
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
        
        Assert.assertEquals(revisionBeforeCommits.add(2), fullHistory.getEndRevision());
        List<OWLOntologyChange> firstChanges = fullHistory.cropChanges(revisionBeforeCommits, revisionBeforeCommits.next()).getChanges(ontology1);
        Assert.assertEquals(1, firstChanges.size());
        Assert.assertEquals(new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION), firstChanges.get(0));
        
        List<OWLOntologyChange> secondChanges = fullHistory.cropChanges(revisionBeforeCommits.add(1), revisionBeforeCommits.add(2)).getChanges(ontology1);
        Assert.assertEquals(0, secondChanges.size());
    }
    
    @Test
    public void testNoSelfConflict() throws OWLOntologyCreationException, OWLServerException {
        setupClient1();
        RemoteOntologyDocument testDoc = vont1.getServerDocument();
        OWLOntology ontology1 = vont1.getOntology();
        Assert.assertFalse(ontology1.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        OntologyDocumentRevision revisionBeforeCommits = vont1.getRevision();
        TestUtilities.rawCommit(client1, testDoc, revisionBeforeCommits, new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        TestUtilities.rawCommit(client1, testDoc, revisionBeforeCommits, new RemoveAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        
        ChangeHistory fullHistory = client1.getChanges(testDoc, OntologyDocumentRevision.START_REVISION.asPointer(), RevisionPointer.HEAD_REVISION);
        
        Assert.assertEquals(revisionBeforeCommits.add(2), fullHistory.getEndRevision());
        List<OWLOntologyChange> firstChanges = fullHistory.cropChanges(revisionBeforeCommits, revisionBeforeCommits.next()).getChanges(ontology1);
        Assert.assertEquals(1, firstChanges.size());
        Assert.assertEquals(new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION), firstChanges.get(0));
        
        List<OWLOntologyChange> secondChanges = fullHistory.cropChanges(revisionBeforeCommits.add(1), revisionBeforeCommits.add(2)).getChanges(ontology1);
        Assert.assertEquals(1, secondChanges.size());
        Assert.assertEquals(new RemoveAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION), secondChanges.get(0));
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
    
    
    private void setupClient1() throws OWLOntologyCreationException, OWLServerException {
        client1 = getClient("redmond", "troglodyte");
        OWLOntology ontology1 = OWLManager.createOWLOntologyManager().createOntology();
        vont1 = ClientUtilities.createServerOntology(client1, SERVER_TEST_ONT, new ChangeMetaData(), ontology1);
    }
    
    private void setupClient2() throws  OWLOntologyCreationException, OWLServerException {
        client2 = getClient("vendetti", "jenny");
    }
    
    private LocalClient getClient(String username, String password) {
        AuthToken token = Authenticator.localLogin(transport, username, password);
        return transport.getClient(token);
    }

}
