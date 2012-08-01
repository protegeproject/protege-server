package org.protege.owl.server.changes.experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.changes.BufferedDocumentFactory;
import org.protege.owl.server.changes.ChangeDocumentUtilities;
import org.protege.owl.server.changes.DocumentFactoryImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class WriteAndReadNCI {
	public static final String ONTOLOGY_LOCATION = "/Users/tredmond/work/Shared/ontologies/NCI/Thesaurus-11.01e-fixed-annotations.owl";
	
	/*
     * Matthew's work should make this a couple of orders of magnitude faster!
     *
	 * Loading ontology
     * Writing history file
     * Took 69705 ms.
     * Take a deep breath...
     * Reading file
     * Took 50898 ms.
     *
     * With buffering...
     * Writing history file
     * Took 62781 ms.
     * Take a deep breath...
     * Reading file
     * Took 173725 ms.
	 */
	public static void main(String[] args) throws Exception {
        OWLOntology ontology = loadOntology();
        DocumentFactoryImpl factory1 = new DocumentFactoryImpl();
	    File f1 = writeAxioms(ontology, factory1);
		breath();
		readAxioms(f1, factory1);
		
		System.out.println("\nWith buffering...");
		DocumentFactory factory2 = new BufferedDocumentFactory(new DocumentFactoryImpl());
		File f2 = writeAxioms(ontology, factory2);
		breath();
		readAxioms(f2, factory2);
	}
	
	public static OWLOntology loadOntology() throws OWLOntologyCreationException {
		System.out.println("Loading ontology");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		return manager.loadOntologyFromOntologyDocument(new File(ONTOLOGY_LOCATION));
	}
	
	public static File writeAxioms(OWLOntology ontology, DocumentFactory factory) throws IOException, OWLOntologyCreationException {
		System.out.println("Writing history file");
		File tmp = File.createTempFile("Thesaurus", ".history");
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (OWLAxiom axiom : ontology.getAxioms()) {
			changes.add(new AddAxiom(ontology, axiom));
		}
		long startTime = System.currentTimeMillis();
		ChangeDocument doc = factory.createChangeDocument(changes, new TreeMap<OntologyDocumentRevision, ChangeMetaData>(),OntologyDocumentRevision.START_REVISION);
		ChangeDocumentUtilities.writeChanges(doc, tmp);
		System.out.println("Took " + (System.currentTimeMillis() - startTime) + " ms.");

		return tmp;
	}
	
	public static void breath() {
		System.out.println("Take a deep breath...");
		System.gc();
		System.gc();
		System.gc();
	}
	
	public static void readAxioms(File tmp, DocumentFactory factory) throws IOException  {
		System.out.println("Reading file");
		long startTime = System.currentTimeMillis();
		ChangeDocumentUtilities.readChanges(factory, tmp, null, null);
		System.out.println("Took " + (System.currentTimeMillis() - startTime) + " ms.");
	}
}
