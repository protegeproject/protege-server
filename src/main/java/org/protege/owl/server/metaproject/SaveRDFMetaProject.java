package org.protege.owl.server.metaproject;

import java.io.File;
import java.net.URISyntaxException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class SaveRDFMetaProject {

    /**
     * @param args
     * @throws URISyntaxException 
     * @throws OWLOntologyCreationException 
     * @throws OWLOntologyStorageException 
     */
    public static void main(String[] args) throws OWLOntologyCreationException, URISyntaxException, OWLOntologyStorageException {
        String output = "build/metaproject.owl";
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = MetaProject.loadMetaProject(manager);
        manager.saveOntology(ontology, new RDFXMLOntologyFormat(), new FileDocumentTarget(new File(output)));
        System.out.println("RDF style metaproject saved at " + output);
    }

}
