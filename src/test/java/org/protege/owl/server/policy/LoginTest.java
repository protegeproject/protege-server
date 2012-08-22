package org.protege.owl.server.policy;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.TestUtilities;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.UserId;
import org.protege.owl.server.api.exception.AuthenticationFailedException;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.semanticweb.owlapi.model.IRI;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class LoginTest {
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
        framework = TestUtilities.startServer("server-basic-config.xml", "metaproject-002.owl");
    }
    
    @AfterMethod
    public void stopServer() throws BundleException {
        framework.stop();
    }
    
    @Test
    public void testLogin() throws RemoteException, NotBoundException {
        Assert.assertNotNull(RMILoginUtility.login(IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/testdirectory/pizza.owl"), "redmond", "troglodyte"));
        Assert.assertNotNull(RMILoginUtility.login("localhost", rmiPort, "redmond", "troglodyte"));
    }
    
    @Test
    public void testGoodLogin() throws NotBoundException, IOException, OWLServerException {
        AuthToken tim = RMILoginUtility.login("localhost", rmiPort, "redmond", "troglodyte");
        RMIClient client = new RMIClient(tim,"localhost", rmiPort);
        client.initialise();
        client.createRemoteDirectory(IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/test"));
        RemoteServerDirectory root = (RemoteServerDirectory) client.getServerDocument(IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/"));
        Assert.assertEquals(1, client.list(root).size());
    }
    
    @Test
    public void testHackedLoginV1() throws NotBoundException, IOException, OWLServerException {
        AuthToken tim = new SimpleAuthToken(new UserId("redmond"));
        RMIClient client = new RMIClient(tim,"localhost", rmiPort);
        client.initialise();
        boolean hackerFailed = false;
        try {
            client.createRemoteDirectory(IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/test"));
        }
        catch (AuthenticationFailedException una) {
            hackerFailed = true;
        }
        Assert.assertTrue(hackerFailed);
    }
    
    @Test
    public void testHackedLoginV2() throws NotBoundException, IOException, OWLServerException {
        RMILoginUtility.login("localhost", rmiPort, "redmond", "troglodyte");
        AuthToken tim = new SimpleAuthToken(new UserId("redmond"));
        RMIClient client = new RMIClient(tim,"localhost", rmiPort);
        client.initialise();
        boolean hackerFailed = false;
        try {
            client.createRemoteDirectory(IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/test"));
        }
        catch (AuthenticationFailedException una) {
            hackerFailed = true;
        }
        Assert.assertTrue(hackerFailed);
    }
}
