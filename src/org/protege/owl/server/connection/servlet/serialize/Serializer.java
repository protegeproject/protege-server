package org.protege.owl.server.connection.servlet.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.protege.owl.server.exception.OntologyConflictException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public interface Serializer {

    public void serialize(OWLOntology ontology, OutputStream stream) throws OWLOntologyStorageException ;
    
    public OWLOntology deserialize(OWLOntologyManager manager, URL url) 
    throws IOException, OntologyConflictException, RemoteQueryException;
    
    public void serializeException(Throwable t);
}
