package org.protege.owl.server.api;


import java.io.IOException;

import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public interface ClientFactory {
    
    boolean hasSuitableMetaData(OWLOntology ontology) throws IOException;
    
    VersionedOntologyDocument getVersionedOntologyDocument(OWLOntology ontology) throws IOException;
    
    RMIClient connectToServer(OWLOntology ontology) throws OWLServerException, IOException;
    
    boolean isSuitable(IRI serverLocation);
    
    Client connectToServer(IRI serverLocation) throws OWLServerException;

}
