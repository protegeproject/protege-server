package org.protege.owl.server;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class RemoteServerTest {
	Framework framework;

	@BeforeMethod
	public void startServer() throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, BundleException, InterruptedException {
		TestUtilities.initializeServerRoot();
		framework = TestUtilities.startServer(new File("src/test/resources/server-basic-config.xml"));
	}
	
	@AfterMethod
	public void stopServer() throws BundleException {
		framework.stop();
	}

	@Test
	public void testConnect() throws InterruptedException {
		Thread.sleep(60000);
	}

}
