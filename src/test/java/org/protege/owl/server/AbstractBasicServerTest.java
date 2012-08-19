package org.protege.owl.server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.RemoteServerDocument;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class AbstractBasicServerTest {
	private Client client;
	private RemoteServerDirectory testDirectory;
	

	protected abstract void startServer() throws OWLServerException;
	
	protected abstract void stopServer() throws OWLServerException;
	
	protected abstract String getServerRoot();

	protected abstract Client createClient() throws OWLServerException;
	
	@BeforeMethod
	public void setup() throws OWLServerException {
		startServer();
		client = createClient() ;
		testDirectory = client.createRemoteDirectory(IRI.create(getServerRoot() + UUID.randomUUID()));
	}
	
	@AfterMethod
	public void cleanup() throws IOException, OWLServerException {
		stopServer();
	}
	
	@Test
	public void testConnect() throws InterruptedException, NotBoundException, IOException, OWLServerException {
		IRI rootServerLocation = IRI.create(getServerRoot());
		RemoteServerDocument doc = client.getServerDocument(rootServerLocation);
		Assert.assertTrue(doc instanceof RemoteServerDirectory);
		RemoteServerDirectory sd = (RemoteServerDirectory) doc;
		Assert.assertEquals(sd.getServerLocation(), rootServerLocation);
	}
	
	@Test
	public void testLoadPizza() throws IOException, OWLOntologyCreationException, OWLServerException {
		VersionedOntologyDocument versionedPizza = loadPizza();
		OWLOntology ontology1 = versionedPizza.getOntology();
		Client client2 = createClient();
		VersionedOntologyDocument versionedOntology2 = ClientUtilities.loadOntology(client2, OWLManager.createOWLOntologyManager(), versionedPizza.getServerDocument());
		OWLOntology ontology2 = versionedOntology2.getOntology();
		Assert.assertEquals(ontology1.getOntologyID(), ontology2.getOntologyID());
		Assert.assertEquals(ontology1.getAxioms(), ontology2.getAxioms());
		Assert.assertEquals(PizzaVocabulary.CHEESEY_PIZZA.getEquivalentClasses(ontology2).size(), 1);
	}
	
	   @Test
	    public void testBackAndForthNoUpdate() throws IOException, OWLOntologyCreationException, OWLServerException {
	        VersionedOntologyDocument versionedPizza1 = loadPizza();
	        OWLOntology ontology1 = versionedPizza1.getOntology();
	        
	        Client client2 = createClient();
	        VersionedOntologyDocument versionedPizza2 = ClientUtilities.loadOntology(client2, OWLManager.createOWLOntologyManager(), versionedPizza1.getServerDocument());
	        OWLOntology ontology2 = versionedPizza2.getOntology();
	        
	        Assert.assertTrue(ontology2.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	        Assert.assertFalse(ontology2.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
	        
	        List<OWLOntologyChange> changes1 = new ArrayList<OWLOntologyChange>();
	        changes1.add(new RemoveAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	        changes1.add(new AddAxiom(ontology1, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
	        ontology1.getOWLOntologyManager().applyChanges(changes1);
	        ClientUtilities.commit(client, new ChangeMetaData("back"), versionedPizza1);
	        
	        ClientUtilities.update(client2, versionedPizza2);
	        Assert.assertFalse(ontology2.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	        Assert.assertTrue(ontology2.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));

	        List<OWLOntologyChange> changes2 = new ArrayList<OWLOntologyChange>();
	        changes2.add(new RemoveAxiom(ontology1, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
	        changes2.add(new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	        ontology1.getOWLOntologyManager().applyChanges(changes2);
	        ClientUtilities.commit(client, new ChangeMetaData("forth"), versionedPizza1);
	        
	        ClientUtilities.update(client, versionedPizza2);
	        Assert.assertTrue(ontology2.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	        Assert.assertFalse(ontology2.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));      
	    }
	
	@Test
	public void testBackAndForthWithUpdate() throws IOException, OWLOntologyCreationException, OWLServerException {
	    VersionedOntologyDocument versionedPizza1 = loadPizza();
	    OWLOntology ontology1 = versionedPizza1.getOntology();
	    
	    Client client2 = createClient();
	    VersionedOntologyDocument versionedPizza2 = ClientUtilities.loadOntology(client2, OWLManager.createOWLOntologyManager(), versionedPizza1.getServerDocument());
	    OWLOntology ontology2 = versionedPizza2.getOntology();
	    
	    Assert.assertTrue(ontology2.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    Assert.assertFalse(ontology2.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
	    
	    List<OWLOntologyChange> changes1 = new ArrayList<OWLOntologyChange>();
	    changes1.add(new RemoveAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    changes1.add(new AddAxiom(ontology1, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
	    ontology1.getOWLOntologyManager().applyChanges(changes1);
        ClientUtilities.commit(client, new ChangeMetaData("back"), versionedPizza1);
	    
	    ClientUtilities.update(client2, versionedPizza2);
	    Assert.assertFalse(ontology2.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    Assert.assertTrue(ontology2.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));

	    ClientUtilities.update(client, versionedPizza1);
	    List<OWLOntologyChange> changes2 = new ArrayList<OWLOntologyChange>();
	    changes2.add(new RemoveAxiom(ontology1, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
	    changes2.add(new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    ontology1.getOWLOntologyManager().applyChanges(changes2);
	    ClientUtilities.commit(client, new ChangeMetaData("forth"), versionedPizza1);
	    
	    ClientUtilities.update(client2, versionedPizza2);
        Assert.assertTrue(ontology2.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        Assert.assertFalse(ontology2.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));	    
	}

	
	public VersionedOntologyDocument loadPizza() throws OWLOntologyCreationException, OWLServerException {
		IRI pizzaLocation = IRI.create(testDirectory.getServerLocation().toString() + "/pizza" + ChangeHistory.CHANGE_DOCUMENT_EXTENSION);
		OWLOntology ontology = PizzaVocabulary.loadPizza();
		VersionedOntologyDocument versionedOntology = ClientUtilities.createServerOntology(client, pizzaLocation, new ChangeMetaData("A tasty pizza"), ontology);
		return versionedOntology;
	}
}
