package org.protege.owl.server.policy;

import static org.protege.owl.server.TestUtilities.REDMOND;
import static org.protege.owl.server.TestUtilities.PASSWORD_MAP;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.TestUtilities;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.ClientFactory;
import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.exception.AuthenticationFailedException;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.AbstractRMIClientFactory;
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
    private int counter=0;
    
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
        Assert.assertNotNull(RMILoginUtility.login("localhost", rmiPort, REDMOND.getUserName(), PASSWORD_MAP.get(REDMOND)));
    }
    
    @Test
    public void testGoodLogin() throws NotBoundException, IOException, OWLServerException {
        AuthToken tim = RMILoginUtility.login("localhost", rmiPort, REDMOND.getUserName(), "troglodyte");
        RMIClient client = new RMIClient(tim,"localhost", rmiPort);
        client.initialise();
        client.createRemoteDirectory(IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/test"));
        RemoteServerDirectory root = (RemoteServerDirectory) client.getServerDocument(IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/"));
        Assert.assertEquals(1, client.list(root).size());
    }
    
    @Test
    public void testHackedLoginV1() throws NotBoundException, IOException, OWLServerException {
        AuthToken tim = new SimpleAuthToken(REDMOND);
        RMIClient client = new RMIClient(tim,"localhost", rmiPort);
        client.initialise();
        Assert.assertFalse(getClientAuthenticated(client));
    }
    
    @Test
    public void testHackedLoginV2() throws NotBoundException, IOException, OWLServerException {
        RMILoginUtility.login("localhost", rmiPort, REDMOND.getUserName(), "troglodyte");
        AuthToken tim = new SimpleAuthToken(REDMOND);
        RMIClient client = new RMIClient(tim,"localhost", rmiPort);
        client.initialise();
        Assert.assertFalse(getClientAuthenticated(client));
    }
    
    @Test
    public void testClientFactoryRelogin() throws OWLServerException, BundleException, IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, InterruptedException {
        IRI serverLocation = IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort);
        final AtomicInteger wrappedLoginCount = new AtomicInteger(0); // its the wrapping not the atomicity that I am using (hacky - sorry)
        ClientFactory clientFactory = new AbstractRMIClientFactory() {
            
            @Override
            protected AuthToken login(IRI serverLocation) throws AuthenticationFailedException {
                try {
                    wrappedLoginCount.incrementAndGet();
                    return login(serverLocation, REDMOND.getUserName(), "troglodyte");
                }
                catch (NotBoundException nbe) {
                    throw new AuthenticationFailedException("Server error", nbe);
                }
                catch (RemoteException re) {
                    throw new AuthenticationFailedException("Server error", re);
                }
            }
        };
        Client client1 = clientFactory.connectToServer(serverLocation);
        Assert.assertTrue(getClientAuthenticated(client1));
        Assert.assertEquals(1, wrappedLoginCount.get());
        Client client2 = clientFactory.connectToServer(serverLocation);
        Assert.assertTrue(getClientAuthenticated(client2));
        Assert.assertEquals(1, wrappedLoginCount.get());
        stopServer();
        startServer();
        Client client3 = clientFactory.connectToServer(serverLocation);
        Assert.assertTrue(getClientAuthenticated(client3));
        Assert.assertEquals(2, wrappedLoginCount.get());
    }
    
    private boolean getClientAuthenticated(Client client) throws OWLServerException {
        boolean validLogin = true;
        try {
            client.createRemoteDirectory(IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/test" + (counter++)));
        }
        catch (AuthenticationFailedException una) {
            validLogin = false;
        }
        return validLogin;
    }
    
}
