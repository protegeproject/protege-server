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

import org.semanticweb.binaryowl.BinaryOWLChangeLogHandler;
import org.semanticweb.binaryowl.BinaryOWLMetadata;
import org.semanticweb.binaryowl.BinaryOWLOntologyChangeLog;
import org.semanticweb.binaryowl.change.OntologyChangeRecordList;
import org.semanticweb.binaryowl.chunk.SkipSetting;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test(groups={"unit.test"})
@SuppressWarnings("deprecation")
public class FormatTest {
    private Logger logger = Logger.getLogger(FormatTest.class.getCanonicalName());
    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private OWLDataFactory datafactory;
    
    @BeforeClass
    @Parameters({ "ontologyFile" })
    public void setup(String ontologyFile) throws OWLOntologyCreationException {
        long startTime = System.currentTimeMillis();
        manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromOntologyDocument(new File(ontologyFile));
        datafactory = manager.getOWLDataFactory();
        logger.fine("Initial ontology load took " + ((System.currentTimeMillis() - startTime)/1000) + " seconds.");
    }
    
    @Test
    public void testSerialization() throws IOException {
        File serializedFile = File.createTempFile("FormatTest", ".ser");
        List<OWLOntologyChange> changes = getChanges();
        writeChanges(changes, serializedFile);
        List<OWLOntologyChange> changes2 = readChanges(serializedFile);
        Assert.assertEquals(changes.size(), changes2.size());
        Assert.assertEquals(changes, changes2);
    }
    
    /**
    @Test
    public void testCompressedSerialization() throws IOException {
        File serializedFile = File.createTempFile("FormatTest", ".ser");
        List<OWLOntologyChange> changes = getChanges();
        writeChangesWithCompression(changes, serializedFile);
        List<OWLOntologyChange> changes2 = readChanges(serializedFile);
        Assert.assertEquals(changes, changes2);
    }
    **/
    
    private List<OWLOntologyChange> getChanges() {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        changes.add(new SetOntologyID(ontology, ontology.getOntologyID()));
        for (OWLAnnotation annotation : ontology.getAnnotations()) {
            changes.add(new AddOntologyAnnotation(ontology, annotation));
        }
        changes.add(new AddImport(ontology, datafactory.getOWLImportsDeclaration(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl"))));
        for (OWLAxiom axiom : ontology.getAxioms()) {
            changes.add(new AddAxiom(ontology, axiom));
        }
        return changes;
    }
    
	private void writeChanges(List<OWLOntologyChange> changes, File output)
			throws IOException {
		long startTime = System.currentTimeMillis();
		OutputStream os = new BufferedOutputStream(new FileOutputStream(output));

		BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();

		log.appendChanges(changes, System.currentTimeMillis(),
				BinaryOWLMetadata.emptyMetadata(), os);

		os.flush();
		os.close();
		logger.fine("Write to history file took "
				+ ((System.currentTimeMillis() - startTime) / 1000)
				+ " seconds.");
	}
    /**
    private void writeChangesWithCompression(List<OWLOntologyChange> changes, File output) throws IOException {
        long startTime = System.currentTimeMillis();
        OutputStream os = new BufferedOutputStream(new FileOutputStream(output));
        OWLOutputStream owlOut = new OWLOutputStream(os);
        owlOut.setCompressionLimit(1);
        owlOut.writeWithCompression(changes);
        os.flush();
        os.close();
        logger.fine("Compressed Write to history file took " + ((System.currentTimeMillis() - startTime)/1000) + " seconds.");
    }
    **/
    
	@SuppressWarnings("unchecked")
	private List<OWLOntologyChange> readChanges(File input) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(input));
		long startTime = System.currentTimeMillis();

		final List<OWLOntologyChange> loc_changes = new ArrayList<OWLOntologyChange>();
		//final ReplaceChangedOntologyVisitor visitor = new ReplaceChangedOntologyVisitor(ontology);

		BinaryOWLOntologyChangeLog log2 = new BinaryOWLOntologyChangeLog();
		log2.readChanges(is, datafactory, new BinaryOWLChangeLogHandler() {
			@Override
			public void handleChangesRead(OntologyChangeRecordList list,
					SkipSetting skipSetting, long filePosition) {
				
				List<OWLOntologyChangeRecord> change_recs = list
						.getChangeRecords();
				
				for (OWLOntologyChangeRecord rec : change_recs) {
					OWLOntologyChangeData dat = rec.getData();
					OWLOntologyChange loc_chan = dat
							.createOntologyChange(ontology);
					loc_changes.add(loc_chan);

				}

			}

		});

		is.close();
		logger.fine("Read of history file took "
				+ ((System.currentTimeMillis() - startTime) / 1000)
				+ " seconds.");
		
		
        return loc_changes;

	}

}
