package org.protege.owl.server.changes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.protege.owl.server.TestVocabulary;
import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.impl.DocumentFactoryImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SerializationTest {
	private Logger logs = Logger.getLogger(SerializationTest.class.getCanonicalName());
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory factory;
	
	private Random r = new Random();

	@BeforeTest
	public void makeTestOntology() throws OWLOntologyCreationException {
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.createOntology(IRI.create(TestVocabulary.NS2));
		factory = manager.getOWLDataFactory();
	}
	
	@Test
	public void testConsecutive() throws OWLOntologyCreationException, IOException, ClassNotFoundException {
		DocumentFactory factory = new DocumentFactoryImpl();
		OWLOntology ontology1 = OWLManager.createOWLOntologyManager().createOntology();
		
		List<OWLOntologyChange> changes1 = new ArrayList<OWLOntologyChange>();
		changes1.add(new AddAxiom(ontology1, TestVocabulary.AXIOM1));
		OntologyDocumentRevision revision1 = new OntologyDocumentRevision(3);
		ChangeDocument doc1 = factory.createChangeDocument(changes1, null, revision1);
		
		List<OWLOntologyChange> changes2 = new ArrayList<OWLOntologyChange>();
		changes2.add(new RemoveAxiom(ontology1, TestVocabulary.AXIOM2));
		OntologyDocumentRevision revision2 = new OntologyDocumentRevision(4);
		ChangeDocument doc2 = factory.createChangeDocument(changes2, null, revision2);
		
		File tmp = File.createTempFile("ServerTest", ".ser");
		logs.info("Using file " + tmp);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmp));
		out.writeObject(doc1);
		out.writeObject(doc2);
		out.flush();
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(tmp));
		ChangeDocument doc3 = (ChangeDocument) in.readObject();
		ChangeDocument doc4 = (ChangeDocument) in.readObject();
		
		Assert.assertEquals(doc1, doc3);
		Assert.assertEquals(doc2, doc4);
		
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
		changes.add(new AddOntologyAnnotation(ontology, factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral("hello world"))));
		changes.add(new RemoveOntologyAnnotation(ontology, factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral("that was fun"))));
		verifyRoundTrip(changes);
	}
	
	@Test
	public void testImports() throws OWLOntologyCreationException, IOException, ClassNotFoundException {
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		IRI iri1 = IRI.create(TestVocabulary.NS);
		IRI iri2 = IRI.create(TestVocabulary.NS2);
		changes.add(new AddImport(ontology, factory.getOWLImportsDeclaration(iri1)));
		changes.add(new RemoveImport(ontology, factory.getOWLImportsDeclaration(iri2)));
		verifyRoundTrip(changes);
	}
	
	private void verifyRoundTrip(List<OWLOntologyChange> changes) throws IOException, ClassNotFoundException, OWLOntologyCreationException {
		DocumentFactoryImpl docFactory = new DocumentFactoryImpl();
		ChangeDocument doc = docFactory.createChangeDocument(changes, null, new OntologyDocumentRevision(r.nextInt()));
		
		File tmp = File.createTempFile("ServerTest", ".ser");
		logs.info("Using file " + tmp);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmp));
		out.writeObject(doc);
		out.flush();
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(tmp));
		ChangeDocument doc2 = (ChangeDocument) in.readObject();
		
		Assert.assertEquals(doc, doc2);
	}


}
