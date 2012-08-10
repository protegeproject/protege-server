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
    public static final String ONTOLOGY_LOCATION = "/home/redmond/work/Shared/ontologies/NCI/Thesaurus-11.01e-fixed-annotations.owl";
    
    /*
     * Matthew's work should make this a couple of orders of magnitude faster!
     *
     * Loading ontology
     * 
     * With buffering...
     *     Writing history file
     *     Took 65360 ms.
     *     Take a deep breath...
     *     Reading file
     *     Took 36669 ms.
     *     Reading a small section of the file
     *     Took 1978 ms.
     * Without buffering...
     *     Writing history file
     *     Took 64446 ms.
     *     Take a deep breath...
     *     Reading file
     *     Took 45355 ms.
     *     Reading a small section of the file
     *     Took 45882 ms.
     *     
     * Loading ontology
     * Without buffering...
     *     Writing history file
     *     Took 4141 ms.
     *     Take a deep breath...
     *     Reading file
     *     Took 4024 ms.
     *     Reading a small section of the file
     *     Took 5643 ms.
     *     Reading file
     *     Took 4636 ms.
     *     Reading a small section of the file
     *     Took 4278 ms.
    *
     * A Matthew-like parser gave an order of magnitude improvement:

     */
    public static void main(String[] args) throws Exception {
        OWLOntology ontology = loadOntology();

        /*
        System.out.println("\nWith buffering...");
        DocumentFactory factory2 = new BufferedDocumentFactory(new DocumentFactoryImpl());
        File f2 = writeAxioms(ontology, factory2);
        breath();
        readAxioms(f2, factory2);
        */
        
        System.out.println("Without buffering...");
        DocumentFactoryImpl factory1 = new DocumentFactoryImpl();
        File f1 = writeAxioms(ontology, factory1);
        breath();
        readAxioms(f1, factory1);
        readAxioms(f1, factory1);
    }
    
    public static OWLOntology loadOntology() throws OWLOntologyCreationException {
        System.out.println("Loading ontology");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        return manager.loadOntologyFromOntologyDocument(new File(ONTOLOGY_LOCATION));
    }
    
    public static File writeAxioms(OWLOntology ontology, DocumentFactory factory) throws IOException, OWLOntologyCreationException {
        System.out.println("\tWriting history file");
        File tmp = File.createTempFile("Thesaurus", ".history");
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLAxiom axiom : ontology.getAxioms()) {
            changes.add(new AddAxiom(ontology, axiom));
        }
        long startTime = System.currentTimeMillis();
        ChangeDocument doc = factory.createChangeDocument(changes, new ChangeMetaData(),OntologyDocumentRevision.START_REVISION);
        ChangeDocumentUtilities.writeChanges(doc, tmp);
        System.out.println("\tTook " + (System.currentTimeMillis() - startTime) + " ms.");

        return tmp;
    }
    
    public static void breath() {
        System.out.println("\tTake a deep breath...");
        System.gc();
        System.gc();
        System.gc();
    }
    
    public static void readAxioms(File tmp, DocumentFactory factory) throws IOException  {
        System.out.println("\tReading file");
        long startTime = System.currentTimeMillis();
        ChangeDocumentUtilities.readChanges(factory, tmp, null, null);
        System.out.println("\tTook " + (System.currentTimeMillis() - startTime) + " ms.");
        System.out.println("\tReading a small section of the file");
        startTime = System.currentTimeMillis();
        ChangeDocumentUtilities.readChanges(factory, tmp, new OntologyDocumentRevision(12345), new OntologyDocumentRevision(13456));
        System.out.println("\tTook " + (System.currentTimeMillis() - startTime) + " ms.");
    }
}
