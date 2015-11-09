package org.protege.owl.server.conflict;

import static org.protege.owl.server.PizzaVocabulary.CHEESEY_PIZZA_DEFINITION;
import static org.protege.owl.server.PizzaVocabulary.HAS_TOPPING_DOMAIN;
import static org.protege.owl.server.PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION;
import static org.protege.owl.server.TestUtilities.REDMOND;
import static org.protege.owl.server.TestUtilities.VENDETTI;
import static org.protege.owl.server.TestUtilities.PASSWORD_MAP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.antlr.runtime.RecognitionException;
import org.protege.owl.server.TestUtilities;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.ConflictException;
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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConflictTest {
    public static final IRI SERVER_TEST_ONT = IRI.create(LocalClient.SCHEME + "://localhost/Test" + ChangeHistory.CHANGE_DOCUMENT_EXTENSION);
    private Server server;
    private LocalTransportImpl transport;
    
    private Client client1, client2;
    private VersionedOntologyDocument vont1, vont2;
    

    @BeforeMethod
    public void startServer() throws IOException, RecognitionException, OWLServerException {
        TestUtilities.initializeServerRoot();
        Server coreServer =  new ServerImpl(TestUtilities.ROOT_DIRECTORY, TestUtilities.CONFIGURATION_DIRECTORY);
        server = new Authenticator(new ConflictManager(coreServer));
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
    public void testConflictingAxiom01() throws OWLOntologyCreationException, OWLServerException {
        setupClient1();
        setupClient2();
        TestUtilities.commit(client1, vont1,
                             new AddAxiom(vont1.getOntology(), CHEESEY_PIZZA_DEFINITION),
                             new AddAxiom(vont1.getOntology(), HAS_TOPPING_DOMAIN));
        boolean foundConflict = false;
        try {
            TestUtilities.commit(client2, vont2,
                                 new AddAxiom(vont2.getOntology(), CHEESEY_PIZZA_DEFINITION),
                                 new AddAxiom(vont2.getOntology(), NOT_CHEESEY_PIZZA_DEFINITION));
        }
        catch (ConflictException ce) {
            foundConflict = true;
        }
        Assert.assertTrue(foundConflict);
    }
    
    private void setupClient1() throws OWLOntologyCreationException, OWLServerException {
        client1 = getClient(REDMOND.getUserName(), PASSWORD_MAP.get(REDMOND));
        OWLOntology ontology1 = OWLManager.createOWLOntologyManager().createOntology();
        vont1 = ClientUtilities.createAndGetServerOntology(client1, SERVER_TEST_ONT, new ChangeMetaData(), ontology1);
    }
    
    private void setupClient2() throws  OWLOntologyCreationException, OWLServerException {
        client2 = getClient(VENDETTI.getUserName(), PASSWORD_MAP.get(VENDETTI));
        RemoteOntologyDocument doc = (RemoteOntologyDocument) client2.getServerDocument(SERVER_TEST_ONT);
        vont2 = ClientUtilities.loadOntology(client2, OWLManager.createOWLOntologyManager(), doc);
    }
    
    private LocalClient getClient(String username, String password) {
        AuthToken token = Authenticator.localLogin(transport, username, password);
        return transport.getClient(token);
    }
    
    
}
