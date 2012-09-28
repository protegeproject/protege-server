package org.protege.owl.server.changes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.protege.owl.server.PizzaVocabulary;
import org.protege.owl.server.TestVocabulary;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class AbstractSerializationTest {
	private Logger logs = Logger.getLogger(AbstractSerializationTest.class.getCanonicalName());
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	
	private Random r = new Random();
	
	protected abstract DocumentFactory createDocumentFactory();

	@BeforeMethod
	public void makeTestOntology() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.createOntology(IRI.create(TestVocabulary.NS2));
		dataFactory = manager.getOWLDataFactory();
	}
	
	@Test
	public void testConsecutive() throws OWLOntologyCreationException, IOException, ClassNotFoundException {
		DocumentFactory documentFactory = createDocumentFactory();
		OWLOntology ontology1 = OWLManager.createOWLOntologyManager().createOntology();
		
		List<OWLOntologyChange> changes1 = new ArrayList<OWLOntologyChange>();
		changes1.add(new AddAxiom(ontology1, TestVocabulary.AXIOM1));
		OntologyDocumentRevision revision1 = new OntologyDocumentRevision(3);
		ChangeHistory doc1 = documentFactory.createChangeDocument(changes1, null, revision1);
		
		List<OWLOntologyChange> changes2 = new ArrayList<OWLOntologyChange>();
		changes2.add(new RemoveAxiom(ontology1, TestVocabulary.AXIOM2));
		OntologyDocumentRevision revision2 = new OntologyDocumentRevision(4);
		ChangeHistory doc2 = documentFactory.createChangeDocument(changes2, null, revision2);
		
		File tmp = File.createTempFile("ServerTest", ".ser");
		logs.fine("Using file " + tmp);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmp));
		out.writeObject(doc1);
		out.writeObject(doc2);
		out.flush();
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(tmp));
		ChangeHistory doc3 = (ChangeHistory) in.readObject();
		ChangeHistory doc4 = (ChangeHistory) in.readObject();
		
		Assert.assertEquals(doc3, doc1);
		Assert.assertEquals(doc4, doc2);
		
	}
	
	@Test
	public void testConsecutiveInterleaved() throws OWLOntologyCreationException, IOException, ClassNotFoundException {
		DocumentFactory documentFactory = createDocumentFactory();
		OWLOntology ontology1 = OWLManager.createOWLOntologyManager().createOntology();
		
		List<OWLOntologyChange> changes1 = new ArrayList<OWLOntologyChange>();
		changes1.add(new AddAxiom(ontology1, TestVocabulary.AXIOM1));
		OntologyDocumentRevision revision1 = new OntologyDocumentRevision(3);
		ChangeHistory doc2 = documentFactory.createChangeDocument(changes1, null, revision1);
		
		List<OWLOntologyChange> changes2 = new ArrayList<OWLOntologyChange>();
		changes2.add(new RemoveAxiom(ontology1, TestVocabulary.AXIOM2));
		OntologyDocumentRevision revision2 = new OntologyDocumentRevision(4);
		ChangeHistory doc4 = documentFactory.createChangeDocument(changes2, null, revision2);
		
		File tmp = File.createTempFile("ServerTest", ".ser");
		logs.fine("Using file " + tmp);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmp));
		Integer int1 = new Integer(42);
		out.writeObject(int1);
		out.writeObject(doc2);
		String str3 = "But can you do it?";
		out.writeObject(str3);
		out.writeObject(doc4);
		Date d5 = new Date();
		out.writeObject(d5);
		out.flush();
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(tmp));
		Integer int6 = (Integer) in.readObject();
		ChangeHistory doc7 = (ChangeHistory) in.readObject();
		String str8 = (String) in.readObject();
		ChangeHistory doc9 = (ChangeHistory) in.readObject();
		Date d10 = (Date) in.readObject();
		
		Assert.assertEquals(int6, int1);
		Assert.assertEquals(doc7, doc2);
		Assert.assertEquals(str8, str3);
		Assert.assertEquals(doc9, doc4);
		Assert.assertEquals(d10, d5);
		
	}
	
	@Test
	public void testAxioms() throws OWLOntologyCreationException, IOException, ClassNotFoundException {
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		changes.add(new AddAxiom(ontology, TestVocabulary.AXIOM1));
		changes.add(new RemoveAxiom(ontology, TestVocabulary.AXIOM2));
		verifyRoundTrip(changes);
	}
	
	@Test
	public void testOntologyAnnotations() throws OWLOntologyCreationException, IOException, ClassNotFoundException {
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		changes.add(new AddOntologyAnnotation(ontology, dataFactory.getOWLAnnotation(dataFactory.getRDFSLabel(), dataFactory.getOWLLiteral("hello world"))));
		changes.add(new RemoveOntologyAnnotation(ontology, dataFactory.getOWLAnnotation(dataFactory.getRDFSLabel(), dataFactory.getOWLLiteral("that was fun"))));
		verifyRoundTrip(changes);
	}
	
	@Test
	public void testImports() throws OWLOntologyCreationException, IOException, ClassNotFoundException {
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		IRI iri1 = IRI.create(TestVocabulary.NS);
		IRI iri2 = IRI.create(TestVocabulary.NS2);
		changes.add(new AddImport(ontology, dataFactory.getOWLImportsDeclaration(iri1)));
		changes.add(new RemoveImport(ontology, dataFactory.getOWLImportsDeclaration(iri2)));
		verifyRoundTrip(changes);
	}
	
	@Test
	public void testEmptyChangeHistory() throws OWLOntologyCreationException, IOException, ClassNotFoundException {
	    verifyRoundTrip(new ArrayList<OWLOntologyChange>());
	}
	
	@Test
	public void testRollingCropped() throws OWLOntologyCreationException, IOException {
	    DocumentFactory factory = createDocumentFactory();
	    testRollingCropped(factory, 100);
	}
	
	private void verifyRoundTrip(List<OWLOntologyChange> changes) throws IOException, ClassNotFoundException, OWLOntologyCreationException {
		DocumentFactory docFactory = createDocumentFactory();
		ChangeHistory doc = docFactory.createChangeDocument(changes, null, new OntologyDocumentRevision(r.nextInt(500)));
		
		File tmp = File.createTempFile("ServerTest", ".ser");
		logs.fine("Using file " + tmp);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmp));
		out.writeObject(doc);
		out.flush();
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(tmp));
		ChangeHistory doc2 = (ChangeHistory) in.readObject();
		
		Assert.assertEquals(doc, doc2);
	}

	
    protected void testRollingCropped(DocumentFactory factory, int interval) throws OWLOntologyCreationException, IOException {
        ChangeHistory doc = getPizzaChanges(factory);
        OntologyDocumentRevision windowEnd;
        for (OntologyDocumentRevision windowStart = doc.getStartRevision();
                windowStart.compareTo(doc.getEndRevision()) < 0;
                windowStart = windowEnd) {
            windowEnd = windowStart.add(interval);
            testCroppedRoundTrip(doc, windowStart, windowEnd);
        }
    }
    
    protected void testCroppedRoundTrip(ChangeHistory doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException {
        DocumentFactory factory = doc.getDocumentFactory();
        File testFile = File.createTempFile("AlignmentRT", ChangeHistory.CHANGE_DOCUMENT_EXTENSION);
        FileOutputStream fos = new FileOutputStream(testFile);
        doc.writeChangeDocument(fos);
        fos.flush();
        fos.close();
        FileInputStream fin = new FileInputStream(testFile);
        ChangeHistory doc2 = factory.readChangeDocument(fin, start, end);
        fin.close();
        Assert.assertEquals(doc2, doc.cropChanges(start, end));
    }

    protected ChangeHistory getPizzaChanges(DocumentFactory factory) throws OWLOntologyCreationException {
        OWLOntology pizzaOntology = PizzaVocabulary.loadPizza();
        ChangeHistory document = factory.createEmptyChangeDocument(OntologyDocumentRevision.START_REVISION);
        for (OWLAxiom axiom : pizzaOntology.getAxioms()) {
            List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
            changes.add(new AddAxiom(pizzaOntology, axiom));
            ChangeHistory toAppend = factory.createChangeDocument(changes, new ChangeMetaData(), document.getEndRevision());
            document = document.appendChanges(toAppend);
        }
        return document;
    }

}
