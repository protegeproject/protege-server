package org.protege.owl.server.changes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.protege.owl.server.PizzaVocabulary;
import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BufferedSerializationTest extends AbstractSerializationTest {
    private int prime1 = 7;
    private int prime2 = 11;

    @Override
    protected BufferedDocumentFactory createDocumentFactory() {
        return new BufferedDocumentFactory(new DocumentFactoryImpl());
    }
    
    // I have some additional tests to add for the BufferedDocumentFactory...
    

    public void testTestConfiguration() throws OWLOntologyCreationException {
        ChangeDocument doc = getPizzaChanges(createDocumentFactory());
        Assert.assertTrue(doc.getEndRevision().getRevision() - doc.getStartRevision().getRevision() > 3 * prime1 * prime2);
        Assert.assertTrue(prime2 > prime1);
        Assert.assertTrue(2 * prime1 > prime2);
    }
    
    @Test
    public void testAllAlignments01() throws OWLOntologyCreationException, IOException {
        BufferedDocumentFactory factory = createDocumentFactory();
        factory.setBufferSize(prime2);
        testRollingCropped(factory, prime1);
    }
    
    @Test
    public void testAllAlignments02() throws OWLOntologyCreationException, IOException {
        BufferedDocumentFactory factory = createDocumentFactory();
        factory.setBufferSize(prime2);
        testRollingCropped(factory, 3 * prime1);
    }
    
    @Test
    public void testAllAlignments03() throws OWLOntologyCreationException, IOException {
        BufferedDocumentFactory factory = createDocumentFactory();
        factory.setBufferSize(prime1);
        testRollingCropped(factory, prime2);
    }
    

}
