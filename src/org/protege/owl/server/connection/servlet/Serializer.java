package org.protege.owl.server.connection.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.protege.owl.server.exception.OntologyConflictException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public interface Serializer {

    void serialize(OWLOntology ontology, OutputStream stream) throws OWLOntologyStorageException ;
    
    OWLOntology deserialize(OWLOntologyManager manager, URL url) 
        throws IOException, OntologyConflictException, RemoteQueryException;
    
    OWLOntology deserialize(OWLOntologyManager manager, OWLOntologyDocumentSource source) throws IOException, RemoteQueryException;
    
}
