package org.protege.owl.server.api;


import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;

public interface ClientFactory {
    
    boolean isSuitable(IRI serverLocation);
    
    Client createClient(IRI serverLocation) throws OWLServerException;

}
