package org.protege.owl.server;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.protege.owl.server.connect.local.LocalClient;
import org.protege.owl.server.impl.ServerImpl;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class LocalServerTest {
	public static final File ROOT_DIRECTORY = new File("build/server.root");

	private Server server;
	private Client client;
	private ClientUtilities clientUtilities;
	private ServerDirectory testDirectory;
	
	@BeforeTest
	public void startServer() throws IOException {
		ROOT_DIRECTORY.delete();
		ROOT_DIRECTORY.mkdirs();
		server = new ServerImpl(ROOT_DIRECTORY);
		client = new LocalClient(null, server);
		clientUtilities = new ClientUtilities(client);
		testDirectory = client.createRemoteDirectory(IRI.create(ServerImpl.SCHEME + "://localhost/" + UUID.randomUUID()));
	}
	
	@Test
	public void testLoadPizza() throws IOException, OWLOntologyCreationException {
		VersionedOWLOntology versionedPizza = loadPizza();
		Client client2 = new LocalClient(null, server);
		ClientUtilities client2Utilities = new ClientUtilities(client2);
		VersionedOWLOntology versionedOntology2 = client2Utilities.loadOntology(OWLManager.createOWLOntologyManager(), versionedPizza.getServerDocument());
		OWLOntology ontology2 = versionedOntology2.getOntology();
		Assert.assertEquals(versionedPizza.getOntology().getAxioms(), ontology2.getAxioms());
		Assert.assertEquals(Pizza.CHEESEY_PIZZA.getEquivalentClasses(ontology2).size(), 1);
	}

	
	public VersionedOWLOntology loadPizza() throws IOException, OWLOntologyCreationException {
		IRI pizzaLocation = IRI.create(testDirectory.getServerLocation().toString() + "/pizza");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(IRI.create(new File(Pizza.PIZZA_LOCATION)));
		VersionedOWLOntology versionedOntology = clientUtilities.createServerOntology(pizzaLocation, "A tasty pizza", ontology);
		return versionedOntology;
	}
}
