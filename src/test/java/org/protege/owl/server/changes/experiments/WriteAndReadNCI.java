package org.protege.owl.server.changes.experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.changes.ChangeDocumentImpl;
import org.protege.owl.server.changes.ChangeDocumentUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class WriteAndReadNCI {
	public static final String ONTOLOGY_LOCATION = "/Users/tredmond/work/Shared/ontologies/NCI/Thesaurus-11.01e-fixed-annotations.owl";
	
	public static void main(String[] args) throws Exception {
		File f = writeAxioms();
		breath();
		readAxioms(f);
	}
	
	public static OWLOntology loadOntology() throws OWLOntologyCreationException {
		System.out.println("Loading ontology");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		return manager.loadOntologyFromOntologyDocument(new File(ONTOLOGY_LOCATION));
	}
	
	public static File writeAxioms() throws IOException, OWLOntologyCreationException {
		OWLOntology ontology = loadOntology();
		System.out.println("Writing history file");
		File tmp = File.createTempFile("Thesaurus", ".history");
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (OWLAxiom axiom : ontology.getAxioms()) {
			changes.add(new AddAxiom(ontology, axiom));
		}
		long startTime = System.currentTimeMillis();
		ChangeDocument doc = new ChangeDocumentImpl(OntologyDocumentRevision.START_REVISION, changes, new TreeMap<OntologyDocumentRevision, ChangeMetaData>());
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
	
	public static void readAxioms(File tmp) throws IOException  {
		System.out.println("Reading file");
		long startTime = System.currentTimeMillis();
		ChangeDocumentUtilities.readChanges(tmp);
		System.out.println("Took " + (System.currentTimeMillis() - startTime) + " ms.");
	}
}
