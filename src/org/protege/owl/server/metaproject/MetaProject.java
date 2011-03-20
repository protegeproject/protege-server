package org.protege.owl.server.metaproject;

import java.net.URISyntaxException;
import java.net.URL;

import org.protege.owl.server.configuration.ServerConfiguration;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class MetaProject {
    public static void addMetaProjectIRIMapper(OWLOntologyManager manager) throws URISyntaxException {
        URL metaproject = getMetaProjectURL();
        manager.addIRIMapper(new SimpleIRIMapper(IRI.create(Vocabulary.NS), IRI.create(metaproject)));
    }
    
    public static OWLOntology loadMetaProject(OWLOntologyManager manager) throws OWLOntologyCreationException, URISyntaxException {
        URL metaproject = getMetaProjectURL();
        return manager.loadOntologyFromOntologyDocument(IRI.create(metaproject));
    }
    
    public static URL getMetaProjectURL() {
        return ServerConfiguration.class.getResource("/metaproject.owl");
    }
}
