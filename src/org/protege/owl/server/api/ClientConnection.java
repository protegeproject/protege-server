package org.protege.owl.server.api;

import java.util.List;
import java.util.Set;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;

public interface ClientConnection extends OWLOntologySetProvider {
    
    OWLOntologyManager getOntologyManager();
    
    Set<ServerOntologyInfo> getRemoteOntologyList(boolean forceUpdate) throws RemoteQueryException;
    
    OWLOntology pull(IRI ontologyName, Integer revision) throws OWLOntologyCreationException, RemoteQueryException;
    
    int getRevision(OWLOntology ontology);
    
    void commit(Set<OWLOntology> ontologies) throws RemoteOntologyChangeException, RemoteQueryException;
    
    void update(OWLOntology ontology, Integer revision) throws OWLOntologyChangeException, RemoteQueryException;
    
    List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology);
    
    void dispose();

}
