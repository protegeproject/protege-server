package org.protege.owl.server;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.client.RemoteServerDirectory;
import org.protege.owl.server.api.client.RemoteServerDocument;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class AbstractBasicServerTest {
    public static final String IMPORT_TEST_DIR = "src/test/resources/import";
    public static final File IMPORTING_LOC = new File(IMPORT_TEST_DIR, "Importer.owl");
    public static final File IMPORTED_LOC = new File(IMPORT_TEST_DIR, "Imported.owl");
    public static final String IMPORTING_NS = "http://protege.org/ImportTest/Importer";
    public static final String IMPORTED_NS  = "http://protege.org/ImportTest/Imported";

    public final IRI importingServerIRI = IRI.create(getServerRoot() + "Importing.history");

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
		Assert.assertEquals(EntitySearcher.getEquivalentClasses(PizzaVocabulary.CHEESEY_PIZZA, ontology2).size(), 2);
	}

	/*
	 * This used to be a test of the ClientUtilities commit code.  But I have changed that code so that it does an update
	 * after any commit.  Therefore in order to get the no update flavor of this test I need to use a raw commit.
	 */
	@Test
	public void testBackAndForthNoUpdate() throws IOException, OWLOntologyCreationException, OWLServerException {
	    VersionedOntologyDocument versionedPizza1 = loadPizza();
	    OWLOntology ontology1 = versionedPizza1.getOntology();

	    Client client2 = createClient();
	    VersionedOntologyDocument versionedPizza2 = ClientUtilities.loadOntology(client2, OWLManager.createOWLOntologyManager(), versionedPizza1.getServerDocument());
	    OWLOntology ontology2 = versionedPizza2.getOntology();

	    Assert.assertTrue(ontology2.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    Assert.assertFalse(ontology2.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));

	    OntologyDocumentRevision currentPizza1Revision = versionedPizza1.getRevision();
	    TestUtilities.rawCommit(client, versionedPizza1.getServerDocument(), currentPizza1Revision,
	                            new RemoveAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION), new AddAxiom(ontology1, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));


	    ClientUtilities.update(client2, versionedPizza2);
	    Assert.assertFalse(ontology2.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    Assert.assertTrue(ontology2.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));

	    TestUtilities.rawCommit(client, versionedPizza1.getServerDocument(), currentPizza1Revision,
	                            new RemoveAxiom(ontology1, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION), new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));

	    ClientUtilities.update(client, versionedPizza2);
	    Assert.assertFalse(ontology2.containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    Assert.assertTrue(ontology2.containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
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

	@Test
	public void testUpdate() throws OWLOntologyCreationException, OWLServerException {
        VersionedOntologyDocument versionedPizza1 = loadPizza();

        Client client2 = createClient();
        VersionedOntologyDocument versionedPizza2 = ClientUtilities.loadOntology(client2, OWLManager.createOWLOntologyManager(), versionedPizza1.getServerDocument());
        OntologyDocumentRevision originalRevision = versionedPizza2.getRevision();
        Assert.assertFalse(versionedPizza2.getOntology().containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        Assert.assertTrue(versionedPizza2.getOntology().containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));

        TestUtilities.rawCommit(client, versionedPizza1.getServerDocument(), versionedPizza1.getRevision(),
                                new AddAxiom(versionedPizza1.getOntology(), PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION), new RemoveAxiom(versionedPizza1.getOntology(), PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        Assert.assertFalse(versionedPizza2.getOntology().containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        Assert.assertTrue(versionedPizza2.getOntology().containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));

        ClientUtilities.update(client2, versionedPizza2);
        Assert.assertTrue(versionedPizza2.getOntology().containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        Assert.assertFalse(versionedPizza2.getOntology().containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        Assert.assertEquals(versionedPizza2.getRevision(), originalRevision.next());
	}

	@Test
	public void testReverseUpdateWithRedundantCommit() throws OWLOntologyCreationException, OWLServerException {
	    RemoteOntologyDocument pizzaDocument = testReverseUpdateWithRedundantCommitSetup();
	    Client client2 = createClient();
	    VersionedOntologyDocument versionedPizza = ClientUtilities.loadOntology(client2, OWLManager.createOWLOntologyManager(), pizzaDocument);
	    Assert.assertTrue(versionedPizza.getOntology().containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        Assert.assertFalse(versionedPizza.getOntology().containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    ClientUtilities.update(client2, versionedPizza, versionedPizza.getRevision().add(-1).asPointer());
	    Assert.assertTrue(versionedPizza.getOntology().containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        Assert.assertFalse(versionedPizza.getOntology().containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    ClientUtilities.update(client2, versionedPizza, versionedPizza.getRevision().add(-1).asPointer());
	    Assert.assertFalse(versionedPizza.getOntology().containsAxiom(PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
        Assert.assertTrue(versionedPizza.getOntology().containsAxiom(PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	}

	private RemoteOntologyDocument testReverseUpdateWithRedundantCommitSetup() throws OWLOntologyCreationException, OWLServerException {
	    VersionedOntologyDocument versionedPizza1 = loadPizza();
	    TestUtilities.rawCommit(client, versionedPizza1.getServerDocument(), versionedPizza1.getRevision(),
	                            new AddAxiom(versionedPizza1.getOntology(), PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION), new RemoveAxiom(versionedPizza1.getOntology(), PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
	    TestUtilities.rawCommit(client, versionedPizza1.getServerDocument(), versionedPizza1.getRevision().next(),
	                            new AddAxiom(versionedPizza1.getOntology(), PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
	    ChangeHistory redundantChangeHistory = client.getChanges(versionedPizza1.getServerDocument(), versionedPizza1.getRevision().next().asPointer(), RevisionPointer.HEAD_REVISION);
	    List<OWLOntologyChange> redundantChangeList = redundantChangeHistory.getChanges(versionedPizza1.getOntology());
	    Assert.assertEquals(redundantChangeList.size(), 1);
	    Assert.assertEquals(redundantChangeList.get(0), new AddAxiom(versionedPizza1.getOntology(), PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION));
	    return versionedPizza1.getServerDocument();
	}

    @Test
    public void testUserIdAdded() throws OWLOntologyCreationException, OWLServerException {
        VersionedOntologyDocument versionedPizza = loadPizza();
        OntologyDocumentRevision revisionBeforeCommit = versionedPizza.getRevision();
        RemoteOntologyDocument testDoc = versionedPizza.getServerDocument();
        OWLOntology ontology1 = versionedPizza.getOntology();
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        changes.add(new AddAxiom(ontology1, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        client.commit(testDoc, client.getDocumentFactory().createChangeDocument(changes, new ChangeMetaData(), versionedPizza.getRevision()));
        ChangeHistory committedChange = client.getChanges(testDoc, revisionBeforeCommit.asPointer(), revisionBeforeCommit.next().asPointer());
        Assert.assertEquals(1, committedChange.getChanges(ontology1).size());
        Assert.assertEquals(client.getUserId(), committedChange.getMetaData(revisionBeforeCommit).getUserId());
    }

    @Test
    public void testEvaluateRevisionPointer() throws OWLOntologyCreationException, OWLServerException {
        VersionedOntologyDocument versionedPizza1 = loadPizza();
        OWLOntology ontology = versionedPizza1.getOntology();
        OntologyDocumentRevision revision = versionedPizza1.getRevision();
        Assert.assertEquals(client.evaluateRevisionPointer(versionedPizza1.getServerDocument(), RevisionPointer.HEAD_REVISION), revision);
        TestUtilities.rawCommit(client, versionedPizza1.getServerDocument(), revision,
                                new AddAxiom(ontology, PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION),
                                new RemoveAxiom(ontology, PizzaVocabulary.CHEESEY_PIZZA_DEFINITION));
        Assert.assertEquals(client.evaluateRevisionPointer(versionedPizza1.getServerDocument(), RevisionPointer.HEAD_REVISION), revision.next());
    }

    @Test
    public void testLoadEmptyServerDocument() throws OWLServerException, OWLOntologyCreationException {
        IRI serverIRI = IRI.create(testDirectory.getServerLocation().toString() + "/empty" + ChangeHistory.CHANGE_DOCUMENT_EXTENSION);
        RemoteOntologyDocument doc = client.createRemoteOntology(serverIRI);
        ClientUtilities.loadOntology(client, OWLManager.createOWLOntologyManager(), doc);
        client.getServerDocument(doc.getServerLocation());
        client.getChanges(doc, OntologyDocumentRevision.START_REVISION.asPointer(), RevisionPointer.HEAD_REVISION);
    }

	protected VersionedOntologyDocument loadPizza() throws OWLOntologyCreationException, OWLServerException {
		IRI pizzaLocation = IRI.create(testDirectory.getServerLocation().toString() + "/pizza" + ChangeHistory.CHANGE_DOCUMENT_EXTENSION);
		OWLOntology ontology = PizzaVocabulary.loadPizza();
		VersionedOntologyDocument versionedOntology = ClientUtilities.createAndGetServerOntology(client, pizzaLocation, new ChangeMetaData("A tasty pizza"), ontology);
		return versionedOntology;
	}

	@Test
	public void testLateImport() throws OWLOntologyCreationException, OWLServerException {
	    testEarlyOrLateImport(false);
	}

	@Test
	public void testEarlyImport() throws OWLOntologyCreationException, OWLServerException {
	    testEarlyOrLateImport(true);
	}

	private void testEarlyOrLateImport(boolean importAlreadyLoaded) throws OWLOntologyCreationException, OWLServerException {
	    installImportingOntology();
	    RemoteOntologyDocument importedDoc = (RemoteOntologyDocument) client.getServerDocument(importingServerIRI);
	    OWLOntologyManager manager = createImportingOntologyManager();
	    if (importAlreadyLoaded) {
	        // here the imported ontology is already loaded before we retrieve the addAxiom from the server.
	        manager.loadOntologyFromOntologyDocument(IMPORTED_LOC);
	    }
	    VersionedOntologyDocument vont = ClientUtilities.loadOntology(client, manager, importedDoc);
	    assertImportFound(vont.getOntology());
	}

	@Test
	public void testUpdateAddImportLate() throws OWLServerException, OWLOntologyCreationException {
	    testUpdateAddImport(false);
	}

	@Test
	public void testUpdateAddImportEarly() throws OWLServerException, OWLOntologyCreationException {
	    testUpdateAddImport(true);
	}

	private void testUpdateAddImport(boolean importAlreadyLoaded) throws OWLServerException, OWLOntologyCreationException {
	    OWLDataFactory factory = OWLManager.getOWLDataFactory();
	    OWLImportsDeclaration decl = factory.getOWLImportsDeclaration(IRI.create(IMPORTED_NS));
	    Client client1 = createClient();
	    RemoteOntologyDocument doc = client1.createRemoteOntology(importingServerIRI);

	    Client client2 = createClient();
	    OWLOntologyManager manager2 = createImportingOntologyManager();
	    VersionedOntologyDocument vont2 = ClientUtilities.loadOntology(client2, manager2, doc);
	    Assert.assertEquals(1, manager2.getOntologies().size());
	    Assert.assertEquals(0, manager2.getOntologies().iterator().next().getImportsDeclarations().size());
	    if (importAlreadyLoaded) {
	        // here the imported ontology is already loaded before we retrieve the addAxiom from the server.
	        manager2.loadOntologyFromOntologyDocument(IMPORTED_LOC);
	        Assert.assertEquals(2, manager2.getOntologies().size());
	    }

	    TestUtilities.rawCommit(client1, doc, OntologyDocumentRevision.START_REVISION, new AddImport(vont2.getOntology(), decl));

	    ClientUtilities.update(client2, vont2);
	    assertImportFound(vont2.getOntology());
	}

	@Test
	public void testReverseUpdateAddImportLate() throws OWLOntologyCreationException, OWLServerException {
	    testReverseUpdateAddImport(false);
	}

	@Test
	public void testReverseUpdateAddImportEarly() throws OWLOntologyCreationException, OWLServerException {
	    testReverseUpdateAddImport(true);
	}

	private void testReverseUpdateAddImport(boolean importAlreadyLoaded) throws OWLServerException, OWLOntologyCreationException {
	    OWLDataFactory factory = OWLManager.getOWLDataFactory();
	    OWLImportsDeclaration decl = factory.getOWLImportsDeclaration(IRI.create(IMPORTED_NS));
	    Client client1 = createClient();
	    RemoteOntologyDocument doc = client1.createRemoteOntology(importingServerIRI);

	    Client client2 = createClient();
	    OWLOntologyManager manager2 = createImportingOntologyManager();
	    VersionedOntologyDocument vont2 = ClientUtilities.loadOntology(client2, manager2, doc);
	    Assert.assertEquals(1, manager2.getOntologies().size());
	    Assert.assertEquals(0, manager2.getOntologies().iterator().next().getImportsDeclarations().size());
	    if (importAlreadyLoaded) {
	        // here the imported ontology is already loaded before we retrieve the addAxiom from the server.
	        manager2.loadOntologyFromOntologyDocument(IMPORTED_LOC);
	        Assert.assertEquals(2, manager2.getOntologies().size());
	    }

	    TestUtilities.rawCommit(client1, doc, OntologyDocumentRevision.START_REVISION, new AddImport(vont2.getOntology(), decl));
	    TestUtilities.rawCommit(client1, doc, OntologyDocumentRevision.START_REVISION.next(), new RemoveImport(vont2.getOntology(), decl));

	    ClientUtilities.update(client2, vont2);
	    Assert.assertEquals(importAlreadyLoaded ? 2 : 1, manager2.getOntologies().size());
	    // a reverse update...
	    ClientUtilities.update(client2, vont2, OntologyDocumentRevision.START_REVISION.next().asPointer());

        Assert.assertEquals(2, manager2.getOntologies().size());
	    assertImportFound(vont2.getOntology());
	}

	private void installImportingOntology() throws OWLServerException, OWLOntologyCreationException {
	    Client founder = createClient();
	    OWLOntologyManager manager = createImportingOntologyManager();
	    OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IMPORTING_LOC);
	    ClientUtilities.createServerOntology(founder, importingServerIRI, new ChangeMetaData(), ontology);
	}

	private OWLOntologyManager createImportingOntologyManager() {
	    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntologyIRIMapper mapper = new SimpleIRIMapper(IRI.create(IMPORTED_NS), IRI.create(IMPORTED_LOC));
        manager.addIRIMapper(mapper);
        return manager;
	}

	private void assertImportFound(OWLOntology importingOntology) {
	    Assert.assertEquals(1, importingOntology.getImportsDeclarations().size());
	    Assert.assertEquals(IRI.create(IMPORTED_NS), importingOntology.getImportsDeclarations().iterator().next().getIRI());
	    Assert.assertEquals(1, importingOntology.getImports().size());
	    Assert.assertEquals(IRI.create(IMPORTED_NS), importingOntology.getImports().iterator().next().getOntologyID().getOntologyIRI().get());
	}
}
