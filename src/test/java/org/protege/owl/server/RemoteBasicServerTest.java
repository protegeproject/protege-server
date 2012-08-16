package org.protege.owl.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.exception.OWLServerException;
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
	protected void startServer() throws OWLServerException {
		try {
			TestUtilities.initializeServerRoot();
			framework = TestUtilities.startServer("server-basic-config.xml", "metaproject-001.owl");
		}
		catch (Exception e) {
			throw new OWLServerException(e.getMessage(),e);
		}
	}

	@Override
	public void stopServer() throws OWLServerException {
		try {
			framework.stop();
			framework.waitForStop(60 * 60 * 1000);
		}
		catch (InterruptedException ie) {
		    throw new OWLServerException(ie);
		}
		catch (BundleException be) {
			throw new OWLServerException(be);
		}
	}

	@Override
	protected String getServerRoot() {
		return RMIClient.SCHEME + "://localhost:" + rmiPort + "/";
	}
	
	@Override
	protected Client createClient() throws OWLServerException {
		try {
			RMIClient client = new RMIClient(null, "localhost", rmiPort);
			client.initialise();
			return client;
		}
		catch (RemoteException re) {
		    throw new OWLServerException(re);
		}
		catch (NotBoundException nbe) {
			throw new OWLServerException(nbe.getMessage(), nbe);
		}
	}

}
