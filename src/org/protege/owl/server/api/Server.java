package org.protege.owl.server.api;

import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;

public interface Server extends OWLOntologySetProvider {
    
    OWLOntologyManager getOntologyManager();
    
    Set<RemoteOntologyRevisions> getOntologyList();
    
    Writer getOntologyWriter(OWLOntologyID id) throws RemoteOntologyCreationException;
    
    List<OWLOntologyChange> getChanges(RemoteOntology version1, RemoteOntology version2);

    void applyChanges(List<OWLOntologyChange> changes) throws RemoteOntologyChangeException;
    
    void setConflictManager(ConflictManager conflictManager);
}
