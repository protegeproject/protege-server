package org.protege.owl.server.command;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_ROOT_PATH;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.STANDARD_SERVER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.protege.owl.server.configuration.MetaprojectVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;


/**
 * Sets the data directory for the a Protege Server metaproject.  This is intended to be 
 * used as a helper command for the ant scripts.
 * 
 * @author tredmond
 *
 */
public class SetMetaprojectDataDir {
    private static final Logger LOGGER = Logger.getLogger(SetMetaprojectDataDir.class.getCanonicalName());

    /**
     * @param args
     * @throws OWLOntologyCreationException 
     * @throws OWLOntologyStorageException 
     */
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
        File metaprojectLocation = new File(args[0]);
        String dataDir = args[1];
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        MetaprojectVocabulary.addIRIMapper(manager);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(metaprojectLocation);
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLDataPropertyAssertionAxiom axiom : ontology.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION)) {
            if (axiom.getProperty().equals(HAS_ROOT_PATH)) {
                OWLIndividual server = axiom.getSubject();
                changes.add(new RemoveAxiom(ontology, axiom));
                changes.add(new AddAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(HAS_ROOT_PATH, server, dataDir)));
            }
        }
        for (OWLIndividual server : STANDARD_SERVER.getIndividuals(ontology)) {
            changes.add(new AddAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(HAS_ROOT_PATH, server, dataDir)));
        }
        manager.applyChanges(changes);
        manager.saveOntology(ontology);
        LOGGER.info("Set data directory to " + dataDir);
    }

}
