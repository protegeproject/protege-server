package org.protege.owl.server.command;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_REGISTRY_PORT;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_SERVER_PORT;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.STANDARD_SERVER;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.semanticweb.owlapi.search.EntitySearcher;


/**
 * Sets the data directory for the a Protege Server metaproject.  This is intended to be 
 * used as a helper command for the ant scripts.
 * 
 * @author tredmond
 *
 */
public class SetMetaProjectPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetMetaProjectPort.class.getCanonicalName());

    /**
     * @param args
     * @throws OWLOntologyCreationException 
     * @throws OWLOntologyStorageException 
     */
    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
        File metaprojectLocation = new File(args[0]);
        int port = Integer.parseInt(args[1]);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        MetaprojectVocabulary.addIRIMapper(manager);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(metaprojectLocation);
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLDataPropertyAssertionAxiom axiom : ontology.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION)) {
            if (axiom.getProperty().equals(HAS_REGISTRY_PORT) || axiom.getProperty().equals(HAS_SERVER_PORT)) {
                OWLIndividual server = axiom.getSubject();
                changes.add(new RemoveAxiom(ontology, axiom));
                changes.add(new AddAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(axiom.getProperty(), server, port)));
            }
        }
        for (OWLIndividual server : EntitySearcher.getIndividuals(STANDARD_SERVER, ontology)) {
            changes.add(new AddAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(HAS_SERVER_PORT, server, port)));
            changes.add(new AddAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(HAS_REGISTRY_PORT, server, port)));
        }
        manager.applyChanges(changes);
        manager.saveOntology(ontology);
        LOGGER.info("Port set to " + port);
    }

}
