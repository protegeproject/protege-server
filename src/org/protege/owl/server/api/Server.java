package org.protege.owl.server.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.protege.owl.server.exception.RemoteOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * This interface defines the server side responsibilities for managing versions of ontologies.  Essentially 
 * it must be able to provide the raw content of `marked' revisions of the ontology and it must be able to
 * provide a set of changes to go from any revision of an ontology to any other revision of that ontology.  
 * Implementations of this class need not concern themselves with the problem of how the server capabilities are
 * made available on the internet - this is done by implementations of the ServerTransport class.
 * 
 * @author tredmond
 *
 */
public interface Server extends OWLOntologySetProvider {
    
    OWLOntologyManager getOntologyManager();
    
    ConflictManager getConflictManager();
    
    void setConflictManager(ConflictManager conflictManager);
    
    Set<RemoteOntologyRevisions> getOntologyList();
    
    InputStream getOntologyStream(IRI ontologyName, int revision) throws RemoteOntologyCreationException;
    
    void save(OWLOntologyID id, int revision, File location) throws IOException, OWLOntologyStorageException;
    
    /**
     * Returns a minimal set of changes required to go from version1 of the ontology with the name ontologyName
     * to version2 of the 
     * ontology.  There are two important reasons why the minimality of the set of changes is important:
     * <ol>
     * <li> The order of the changes is unimportant.</li>
     * <li> The changes to go from version2 of the ontology to version1 of the ontology can be calculated by
     *      simply replacing the add axiom/import/ontology annotation with the remove axiom/import/ontology annotation
     *      calls and vice-versa.</li>
     * </ol>
     * As usual the setOntologyID calls are not recorded.
     * 
     * @param ontologyName the name of the ontology whose changes are being calculated.
     * @param version1 the starting revision of the ontology
     * @param version2 the ending revision of the ontology
     * @return a minimal collection of changes needed to go from version1 to version2 of the ontology.
     */
    List<OWLOntologyChange> getChanges(IRI ontologyName, int version1, int version2) throws RemoteOntologyException;

    void applyChanges(Map<IRI, Integer> versions, List<OWLOntologyChange> changes) throws RemoteOntologyChangeException;

    List<OWLOntologyChange> reduceChangeList(Map<IRI, Integer> versions, List<OWLOntologyChange> changes);
}
