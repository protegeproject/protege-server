package org.protege.owl.server;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.connect.local.LocalClient;
import org.protege.owl.server.impl.ServerImpl;
import org.semanticweb.owlapi.model.IRI;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

public class LocalServerTest {
	public static final File ROOT_DIRECTORY = new File("build/server.root");
	
	private Client client;
	private ServerDirectory testDirectory;

	@BeforeSuite
	public void makeRootDir() {
		ROOT_DIRECTORY.mkdirs();
	}
	
	@BeforeTest
	public void startServer() throws IOException {
		Server server = new ServerImpl(ROOT_DIRECTORY);
		client = new LocalClient(null, server);
		testDirectory = client.createRemoteDirectory(IRI.create(ServerImpl.SCHEME + "//localhost/" + UUID.randomUUID()));
	}
}
