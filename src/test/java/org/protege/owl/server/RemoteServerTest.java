package org.protege.owl.server;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
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
	public void testConnect() throws InterruptedException {
		;
	}

}
