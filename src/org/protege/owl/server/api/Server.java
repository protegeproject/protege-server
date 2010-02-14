package org.protege.owl.server.api;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.protege.owl.server.exception.RemoteOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;

public interface Server extends OWLOntologySetProvider {
    
    OWLOntologyManager getOntologyManager();
    
    Set<RemoteOntologyRevisions> getOntologyList();
    
    Writer getOntologyWriter(RemoteOntology ontology) throws RemoteOntologyCreationException;
    
    void save(IRI version, File location) throws IOException;
    
    List<OWLOntologyChange> getChanges(RemoteOntology version1, RemoteOntology version2) throws RemoteOntologyException;

    void applyChanges(List<OWLOntologyChange> changes) throws RemoteOntologyChangeException;
    
    void setConflictManager(ConflictManager conflictManager);
}
