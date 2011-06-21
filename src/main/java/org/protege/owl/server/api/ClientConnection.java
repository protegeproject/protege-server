package org.protege.owl.server.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologySetProvider;

/**
 * This class represents the result of a connection between a client and a server.  There may be several implementation of 
 * the client connection class (e.g. a restful client connection, an rmi client connection, etc).  In order to be able to perform
 * correctly thought the ClientConnection instance used by the client must correspond to the ServerConnection instance present on 
 * the server.  In other words, a restful client connection will not be able to successfully connect to a server running an rmi-based 
 * ServerConnection.
 * <p>
 * The current status is  that we have a Server and Client Connection implementation based on restful services.  This should really
 * be based on OWLLink - I think but this hasn't happened yet.
 * <p>
 * @author tredmond
 *
 */
public interface ClientConnection extends OWLOntologySetProvider {
    
	/**
	 * A ClientConnection holds all the remote ontologies in a single OWLOntology Manager.
	 * 
	 * @return the OWLOntologyManager for all the ontologies  associated with this ClientConnection.
	 */
    ProtegeOWLOntologyManager getOntologyManager();
    
    /**
     * This routine retrieves some information about the ontologies held on the server side indexed by their name.
     * It is currently assumed by this implementation that there is only one server side ontology with any given 
     * IRI.  The version IRI is used only to distinguish different copies of a server side that get saved on the server.
     * 
     * @param forceUpdate If true then ignore any local cache of the ontology information and get the 
     *         latest information from the server
     * @return a map from ontology names to some information about the ontology including its IRI, its latest version
     *         number and its short name.
     * @throws RemoteQueryException
     */
    Map<IRI, ServerOntologyInfo> getOntologyInfoByIRI(boolean forceUpdate) throws RemoteQueryException;
    
    /**
     * Every ontology on the server side also has a short name which is intended to be something that can be used in such things
     * as a part of a path in a restful service or as the name of a table in a database.  This routine retrieves some information 
     * about the server side ontologies indexed by their short nanme. 
     * @param forceUpdate If true then ignore any local cache of the ontology information and get the 
     *         latest information from the server
     * @return a map from short names to some information about the ontology including its IRI, its latest version
     *         number and its short name.
     * @throws RemoteQueryException
     */
    Map<String, ServerOntologyInfo> getOntologyInfoByShortName(boolean forceUpdate) throws RemoteQueryException;
    
    /**
     * This routine downloads and loads an ontology from the server at a given revision on the client.
     * @param ontologyName the name of the ontology to be downloaded
     * @param revision the revision to use. If the revision is null then this routine should get the latest version of the ontology
     *                from the server.
     * @return the OWLOntology object representing the ontology.
     * @throws OWLOntologyCreationException
     * @throws RemoteQueryException
     */
    OWLOntology pull(IRI ontologyName, Integer revision) throws OWLOntologyCreationException, RemoteQueryException;
    
    /**
     * Get the current (client-side) revision of any ontology 
     * @param ontology
     * @return
     */
    int getClientRevision(OWLOntology ontology);
    
    
    /**
     * Set the client side revision of the ontology.  This should be used with care only
     * when the caller knows that the given ontology corresponds to the server side ontology
     * at a specific revision number.
     * @param ontology
     * @param revision
     */
    
    void setClientRevision(OWLOntology ontology, int revision) throws RemoteQueryException;
    
    /**
     * This call takes the set of local (client-side) changes made to the set of ontologies and commits them on the server.
     * In addition, a consequence of making this call is that the client is brought up to date with the state of the server 
     * at the point the changes were committed.
     * 
     * @param ontologies the ontologies with local changes that are to be committed to the server.
     * @throws RemoteOntologyChangeException one expample of afe
     * @throws RemoteQueryException
     */
    void commit(Set<OWLOntology> ontologies) throws RemoteOntologyChangeException, RemoteQueryException;
    
    /**
     * This call is like the call above except that the client also provides a set of changes - not found
     * in the uncommitted changes to the ontologies - that also need to be committed. 
     */
    
    void commit(Set<OWLOntology> ontologies, List<OWLOntologyChange> changes) throws RemoteOntologyChangeException, RemoteQueryException;
    
    /**
     * Updates the client side copy  of the given  ontology to include server side changes at revision.
     * @param ontology An ontology managed by this ClientConnection
     * @param revision the server side revision number to go to.
     * @throws OWLOntologyChangeException
     * @throws RemoteQueryException
     */
    void update(OWLOntology ontology, Integer revision) throws OWLOntologyChangeException, RemoteQueryException;
    
    /**
     * Obtains  a list of ontology updates from the server that have  not yet been seen on the client.
     * @param ontology an ontology managed by this ClientConnection.
     * @return
     */
    List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology);
    
    /**
     * 
     * @return true if an update from the server is currently in progress.
     */
    boolean isUpdateFromServer();
    
    void dispose();

}
