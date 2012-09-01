package org.protege.owl.server.changes.format;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@SuppressWarnings("deprecation")
public class FormatTest {
    private Logger logger = Logger.getLogger(FormatTest.class.getCanonicalName());
    private OWLOntology ontology;
    
    @BeforeClass
    @Parameters({ "ontologyFile" })
    public void setup(String ontologyFile) throws OWLOntologyCreationException {
        long startTime = System.currentTimeMillis();
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument(new File(ontologyFile));
        logger.fine("Initial ontology load took " + ((System.currentTimeMillis() - startTime)/1000) + " seconds.");
    }
    
    @Test
    public void testSerialization() throws IOException {
        File serializedFile = File.createTempFile("FormatTest", ".ser");
        List<OWLOntologyChange> changes = getChanges();
        writeChanges(changes, serializedFile);
        List<OWLOntologyChange> changes2 = readChanges(serializedFile);
        Assert.assertEquals(changes, changes2);
    }
    
    private List<OWLOntologyChange> getChanges() {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        changes.add(new SetOntologyID(ontology, ontology.getOntologyID()));
        for (OWLAnnotation annotation : ontology.getAnnotations()) {
            changes.add(new AddOntologyAnnotation(ontology, annotation));
        }
        changes.add(new AddImport(ontology, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLImportsDeclaration(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl"))));
        for (OWLAxiom axiom : ontology.getAxioms()) {
            changes.add(new AddAxiom(ontology, axiom));
        }
        return changes;
    }
    
    private void writeChanges(List<OWLOntologyChange> changes, File output) throws IOException {
        long startTime = System.currentTimeMillis();
        OutputStream os = new BufferedOutputStream(new FileOutputStream(output));
        new OWLOutputStream(os).write(changes);
        os.flush();
        os.close();
        logger.fine("Write to history file took " + ((System.currentTimeMillis() - startTime)/1000) + " seconds.");
    }
    
    @SuppressWarnings("unchecked")
    private List<OWLOntologyChange> readChanges(File input) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(input));
        long startTime = System.currentTimeMillis();        
        List<OWLOntologyChange> changes = (List<OWLOntologyChange>) new OWLInputStream(is).read();
        is.close();
        logger.fine("Read of history file took " + ((System.currentTimeMillis() - startTime)/1000) + " seconds.");
        ReplaceChangedOntologyVisitor visitor = new ReplaceChangedOntologyVisitor(ontology);
        List<OWLOntologyChange> adjustedChanges = new ArrayList<OWLOntologyChange>();
        for (OWLOntologyChange change : changes) {
            adjustedChanges.add(change.accept(visitor));
        }
        return adjustedChanges;
    }

}
