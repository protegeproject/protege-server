package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.OWLOntology;

public interface ServerFactory {
    Server createServer(OWLOntology serverConfiguration);
    
    ServerConnection createServerConnection(OWLOntology serverConfiguration);
}
