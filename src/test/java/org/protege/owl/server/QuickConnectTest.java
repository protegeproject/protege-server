package org.protege.owl.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.RootUtils;
import org.protege.owl.server.connect.rmi.AbstractRMIClientFactory;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.util.ClientRegistry;
import org.semanticweb.owlapi.model.IRI;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class QuickConnectTest {
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
    public void testQuickConnect() throws OWLServerException, URISyntaxException {
        TestRMIClientFactory factory = new TestRMIClientFactory();
        Client firstClient = factory.connectToServer(getServerLocation());
        Assert.assertNotNull(firstClient);
        Assert.assertTrue(factory.getReadyConnections().contains(RootUtils.getRoot(getServerLocation())));
        Assert.assertTrue(factory.hasReadyConnection(getServerLocation()));
        Client secondClient = factory.quickConnectToServer(getServerLocation());
        Assert.assertNotNull(secondClient);
    }
    
    @Test
    public void testQuickConnect2() throws OWLServerException, URISyntaxException {
        TestRMIClientFactory rfactory = new TestRMIClientFactory();
        ClientRegistry registry = new ClientRegistry();
        registry.addFactory(rfactory);
        
        Client firstClient = registry.connectToServer(getServerLocation());
        Assert.assertNotNull(firstClient);
        Assert.assertTrue(registry.getReadyConnections().contains(RootUtils.getRoot(getServerLocation())));
        Assert.assertTrue(registry.hasReadyConnection(getServerLocation()));
        Client secondClient = registry.quickConnectToServer(getServerLocation());
        Assert.assertNotNull(secondClient);
    }
    
    private IRI getServerLocation() {
        return IRI.create(RMIClient.SCHEME + "://localhost:" + rmiPort + "/test" + (++counter));
    }

    private static class TestRMIClientFactory extends AbstractRMIClientFactory {
        boolean firstTime=true;
        protected AuthToken login(IRI serverLocation) throws org.protege.owl.server.api.exception.AuthenticationFailedException {
            try {
                if (firstTime) {
                    return login(serverLocation, TestUtilities.REDMOND.getUserName(), TestUtilities.PASSWORD_MAP.get(TestUtilities.REDMOND));
                }
                else {
                    Assert.fail();
                    return null;
                }
            }
            catch (RemoteException e) {
                Assert.fail();
                return null;
            }
            catch (NotBoundException e) {
                Assert.fail();
                return null;
            }
            finally {
                firstTime = false;
            }
        }
    }
}
