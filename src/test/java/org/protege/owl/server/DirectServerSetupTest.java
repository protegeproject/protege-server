package org.protege.owl.server;

import static org.protege.owl.server.TestUtilities.REDMOND;
import static org.protege.owl.server.TestUtilities.PASSWORD_MAP;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.antlr.runtime.RecognitionException;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerTransport;
import org.protege.owl.server.changes.api.RemoteServerDirectory;
import org.protege.owl.server.changes.api.RemoteServerDocument;
import org.protege.owl.server.conflict.ConflictManager;
import org.protege.owl.server.connect.local.LocalTransport;
import org.protege.owl.server.connect.local.LocalTransportImpl;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.connect.rmi.RMITransport;
import org.protege.owl.server.core.ServerImpl;
import org.protege.owl.server.policy.Authenticator;
import org.protege.owl.server.policy.RMILoginUtility;
import org.semanticweb.owlapi.model.IRI;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class DirectServerSetupTest {
    private Server server;
    private LocalTransport localTransport;

    @Parameters({ "rmiPort" })
    @BeforeMethod
    public void startServer(int rmiPort) throws IOException, RecognitionException, OWLServerException {
        TestUtilities.initializeServerRoot();
        
        Server core = new ServerImpl(TestUtilities.ROOT_DIRECTORY, TestUtilities.CONFIGURATION_DIRECTORY);
        server = new Authenticator(new ConflictManager(core));
        
        List<ServerTransport> transports = new ArrayList<ServerTransport>();
        ServerTransport rmiTransport = new RMITransport(rmiPort, rmiPort);
        rmiTransport.start(server);
        transports.add(rmiTransport);
        localTransport = new LocalTransportImpl();
        localTransport.start(server);
        transports.add(localTransport);
        
        server.setTransports(transports);
    }
    
    @AfterMethod
    public void stopServer() {
        server.shutdown();
    }
    
    @Test
    public void accessLocally() throws OWLServerException {
        AuthToken token = Authenticator.localLogin(localTransport, REDMOND.getUserName(), PASSWORD_MAP.get(REDMOND));
        Client client = localTransport.getClient(token);
        checkClientOk(client);
    }

    @Parameters({ "rmiPort" })
    @Test
    public void accessRemotely(int rmiPort) throws OWLServerException, RemoteException, NotBoundException {
        AuthToken tim = RMILoginUtility.login("localhost", rmiPort, REDMOND.getUserName(), PASSWORD_MAP.get(REDMOND));
        RMIClient client = new RMIClient(tim, "localhost", rmiPort);
        client.initialise();
        checkClientOk(client);
    }
    
    
    private void checkClientOk(Client client) throws OWLServerException {
        IRI root = IRI.create(client.getScheme() + "://" + client.getAuthority());
        RemoteServerDocument doc = client.getServerDocument(root);
        Assert.assertTrue(doc instanceof RemoteServerDirectory);
        Assert.assertTrue(client.list((RemoteServerDirectory) doc).isEmpty());
    }
}
