package org.protege.owl.server.api;

import java.util.List;
import java.util.Map;
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
    
    Map<IRI, ServerOntologyInfo> getOntologyInfoByIRI(boolean forceUpdate) throws RemoteQueryException;
    
    Map<String, ServerOntologyInfo> getOntologyInfoByShortName(boolean forceUpdate) throws RemoteQueryException;
    
    OWLOntology pull(IRI ontologyName, Integer revision) throws OWLOntologyCreationException, RemoteQueryException;
    
    int getRevision(OWLOntology ontology);
    
    /**
     * This call takes the set of local (client-side) changes made to the set of ontologies and commits them on the server.
     * In addition, a consequence of making this call is that the client is brought up to date with the state of the server 
     * at the point the changes were committed.
     * 
     * @param ontologies the ontologies with local changes that are to be committed to the server.
     * @throws RemoteOntologyChangeException
     * @throws RemoteQueryException
     */
    void commit(Set<OWLOntology> ontologies) throws RemoteOntologyChangeException, RemoteQueryException;
    
    void update(OWLOntology ontology, Integer revision) throws OWLOntologyChangeException, RemoteQueryException;
    
    List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology);
    
    void dispose();

}
