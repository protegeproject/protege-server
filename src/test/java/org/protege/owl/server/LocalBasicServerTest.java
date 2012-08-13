package org.protege.owl.server;

import java.io.IOException;

import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.connect.local.LocalClient;
import org.protege.owl.server.impl.ServerImpl;

public class LocalBasicServerTest extends AbstractBasicServerTest {
	private Server server;

	@Override
	public void startServer() {
		TestUtilities.initializeServerRoot();
		server = new ServerImpl(TestUtilities.ROOT_DIRECTORY, TestUtilities.CONFIGURATION_DIRECTORY);
	}
	
	@Override
	protected void stopServer() throws IOException {
		server.shutdown();
	}
	
	@Override
	protected String getServerRoot() {
		return LocalClient.SCHEME + "://localhost/";
	}
	
	@Override
	public Client createClient() {
		return new LocalClient(null, server);
	}
	
	
}
