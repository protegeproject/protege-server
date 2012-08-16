package org.protege.owl.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.exception.ServerException;
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
	protected void startServer() throws ServerException {
		try {
			TestUtilities.initializeServerRoot();
			framework = TestUtilities.startServer("server-basic-config.xml", "metaproject-001.owl");
		}
		catch (Exception e) {
			throw new ServerException(e.getMessage(),e);
		}
	}

	@Override
	public void stopServer() throws ServerException {
		try {
			framework.stop();
			framework.waitForStop(60 * 60 * 1000);
		}
		catch (InterruptedException ie) {
		    throw new ServerException(ie);
		}
		catch (BundleException be) {
			throw new ServerException(be);
		}
	}

	@Override
	protected String getServerRoot() {
		return RMIClient.SCHEME + "://localhost:" + rmiPort + "/";
	}
	
	@Override
	protected Client createClient() throws ServerException {
		try {
			RMIClient client = new RMIClient(null, "localhost", rmiPort);
			client.initialise();
			return client;
		}
		catch (RemoteException re) {
		    throw new ServerException(re);
		}
		catch (NotBoundException nbe) {
			throw new ServerException(nbe.getMessage(), nbe);
		}
	}

}
