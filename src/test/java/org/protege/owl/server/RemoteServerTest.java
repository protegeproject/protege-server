package org.protege.owl.server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.semanticweb.owlapi.model.IRI;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class RemoteServerTest {
	private Framework framework;
	private int rmiPort;

	@Parameters({ "rmiPort" })
	@BeforeMethod
	public void startServer(int rmiPort) throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, BundleException, InterruptedException {
		this.rmiPort = rmiPort;
		TestUtilities.initializeServerRoot();
		framework = TestUtilities.startServer("server-basic-config.xml", "metaproject-001.owl");
	}
	
	@AfterMethod
	public void stopServer() throws BundleException {
		framework.stop();
	}

	@Test
	public void testConnect() throws InterruptedException, RemoteException, NotBoundException {
		RMIClient client = new RMIClient("localhost", rmiPort);
		client.initialise();
		IRI rootServerLocation = IRI.create("owlserver://localhost/");
		ServerDocument doc = client.getServerDocument(rootServerLocation);
		Assert.assertTrue(doc instanceof ServerDirectory);
		ServerDirectory sd = (ServerDirectory) doc;
		Assert.assertEquals(sd.getServerLocation(), rootServerLocation);
	}

}
