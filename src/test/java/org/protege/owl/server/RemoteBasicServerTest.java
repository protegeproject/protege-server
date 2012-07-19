package org.protege.owl.server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

public class RemoteBasicServerTest extends AbstractBasicServerTest {
	private Framework framework;
	private int rmiPort;

	@Parameters({ "rmiPort" })
	@BeforeClass
	public void getServerPort(int rmiPort) {
		this.rmiPort = rmiPort;

	}
	
	@Override
	protected void startServer() throws IOException {
		try {
			TestUtilities.initializeServerRoot();
			framework = TestUtilities.startServer("server-basic-config.xml", "metaproject-001.owl");
		}
		catch (IOException ioe) {
			throw ioe;
		}
		catch (Exception e) {
			IOException ioe = new IOException(e.getMessage());
			ioe.initCause(e);
			throw ioe;
		}
	}

	@Override
	public void stopServer() throws IOException {
		try {
			framework.stop();
		}
		catch (BundleException be) {
			throw new IOException(be);
		}
	}


	
	@Override
	protected Client createClient() throws RemoteException {
		try {
		RMIClient client = new RMIClient("localhost", rmiPort);
		client.initialise();
		return client;
		}
		catch (NotBoundException nbe) {
			throw new RemoteException(nbe.getMessage(), nbe);
		}
	}

}
