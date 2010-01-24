package org.protege.owl.server.api;

import java.util.List;
import java.util.Set;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;

public interface Client extends OWLOntologySetProvider {
    
    OWLOntologyManager getOntologyManager();
    
    Set<RemoteOntologyRevisions> getRemoteOntologyList();

    OWLOntology pull(RemoteOntology specification) throws RemoteOntologyCreationException;
    
    RemoteOntology getRevision(OWLOntology ontology);
    
    void commit(OWLOntology ontology) throws RemoteOntologyChangeException;
    
    boolean isConnected(OWLOntology ontology);
    
    boolean setConnected(OWLOntology ontology, boolean connected);
    
    List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology);

}
