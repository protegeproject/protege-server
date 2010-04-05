package org.protege.owl.server.api;

import java.util.List;
import java.util.Set;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;

public interface ClientConnection extends OWLOntologySetProvider {
    
    OWLOntologyManager getOntologyManager();
    
    Set<RemoteOntologyRevisions> getRemoteOntologyList() throws RemoteOntologyCreationException;

    OWLOntology pull(IRI ontologyName, Integer revision) throws RemoteOntologyCreationException;
    
    int getRevision(OWLOntology ontology);
    
    void commit(OWLOntology ontology) throws RemoteOntologyChangeException;
    
    void update(OWLOntology ontology, Integer revision) throws OWLOntologyChangeException;
    
    List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology);
    
    void dispose();

}
