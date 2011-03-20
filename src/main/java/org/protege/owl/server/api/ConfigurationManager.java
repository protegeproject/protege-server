package org.protege.owl.server.api;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

public interface ConfigurationManager {

    void setMetaOntology(OWLOntology ontology);
    
    Set<ServerBuilder> getServerBuilders();
    
    void addServerFactory(ServerFactory factory);
    
    void removeServerFactory(ServerFactory factory);
    
    void start();
    
    void stop();
}
