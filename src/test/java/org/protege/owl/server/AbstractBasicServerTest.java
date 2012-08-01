package org.protege.owl.server;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.UUID;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class AbstractBasicServerTest {
	private Client client;
	private ClientUtilities clientUtilities;
	private ServerDirectory testDirectory;
	

	protected abstract void startServer() throws IOException;
	
	protected abstract void stopServer() throws IOException;
	
	protected abstract String getServerRoot();

	protected abstract Client createClient() throws IOException;
	
	@BeforeMethod
	public void setup() throws IOException {
		startServer();
		client = createClient() ;
		clientUtilities = new ClientUtilities(client);
		testDirectory = client.createRemoteDirectory(IRI.create(getServerRoot() + UUID.randomUUID()));
	}
	
	@AfterMethod
	public void cleanup() throws IOException {
		stopServer();
	}
	
	@Test
	public void testConnect() throws InterruptedException, NotBoundException, IOException {
		IRI rootServerLocation = IRI.create("owlserver://localhost/");
		ServerDocument doc = client.getServerDocument(rootServerLocation);
		Assert.assertTrue(doc instanceof ServerDirectory);
		ServerDirectory sd = (ServerDirectory) doc;
		Assert.assertEquals(sd.getServerLocation(), rootServerLocation);
	}
	
	@Test
	public void testLoadPizza() throws IOException, OWLOntologyCreationException {
		VersionedOWLOntology versionedPizza = loadPizza();
		OWLOntology ontology1 = versionedPizza.getOntology();
		Client client2 = createClient();
		ClientUtilities client2Utilities = new ClientUtilities(client2);
		VersionedOWLOntology versionedOntology2 = client2Utilities.loadOntology(OWLManager.createOWLOntologyManager(), versionedPizza.getServerDocument());
		OWLOntology ontology2 = versionedOntology2.getOntology();
		Assert.assertEquals(ontology1.getOntologyID(), ontology2.getOntologyID());
		Assert.assertEquals(ontology1.getAxioms(), ontology2.getAxioms());
		Assert.assertEquals(PizzaVocabulary.CHEESEY_PIZZA.getEquivalentClasses(ontology2).size(), 1);
	}

	
	public VersionedOWLOntology loadPizza() throws IOException, OWLOntologyCreationException {
		IRI pizzaLocation = IRI.create(testDirectory.getServerLocation().toString() + "/pizza" + ChangeDocument.CHANGE_DOCUMENT_EXTENSION);
		OWLOntology ontology = PizzaVocabulary.loadPizza();
		VersionedOWLOntology versionedOntology = clientUtilities.createServerOntology(pizzaLocation, new ChangeMetaData("A tasty pizza"), ontology);
		return versionedOntology;
	}
}
