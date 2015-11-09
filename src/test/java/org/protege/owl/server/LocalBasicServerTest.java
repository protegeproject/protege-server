package org.protege.owl.server;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.UserId;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.connect.local.LocalClient;
import org.protege.owl.server.core.ServerImpl;

public class LocalBasicServerTest extends AbstractBasicServerTest {
	private Server server;

	@Override
	public void startServer() {
		TestUtilities.initializeServerRoot();
		server = new ServerImpl(TestUtilities.ROOT_DIRECTORY, TestUtilities.CONFIGURATION_DIRECTORY);
	}
	
	@Override
	protected void stopServer() throws OWLServerException {
		server.shutdown();
	}
	
	@Override
	protected String getServerRoot() {
		return LocalClient.SCHEME + "://localhost/";
	}
	
	@Override
	public Client createClient() {
		return new LocalClient(new AuthToken() {
            
            @Override
            public int compareTo(AuthToken o) {
                return getUserId().compareTo(o.getUserId());
            }
            
            @Override
            public UserId getUserId() {
                return new UserId("redmond");
            }
        }, server);
	}
	
	
}
