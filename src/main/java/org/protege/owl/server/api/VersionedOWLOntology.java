package org.protege.owl.server.api;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This is an open OWL ontology held by the client with an association to a corresponding server
 * document at a particular revision.  
 * <p/>
 * There is enough information here that several operations are possible:
 * <ul>
 * <li>the changes made to the local in-memory ontology can be committed to the remote server.  
 *     The collection of changes can be calculated by getting the changes up to the revision of the in-memory
 *     ontology and calculating the changes made since the changes described on the server.  ClientUtilities uses 
 *     just such a method internally.</li>
 * <li>the changes made on the server since the revision can be merged into the clients copy of the ontology</li>
 * </ul>
 * <p/>
 * This interface also supports the idea of a local cache of the history held on the server.  This cache can be updated
 * as change data is collected from the server.
 * 
 * @author redmond
 *
 */
public interface VersionedOWLOntology {

	 OWLOntology getOntology();

	 RemoteOntologyDocument getServerDocument();
	
	 /**
	  * This returns a change document which is a copy of the server-side change document
	  * from revision zero to the current revision (getRevision()) of this document.
	  * <p/>
	  * The idea is that the client will keep a cache of the history locally so that certain 
	  * operations (e.g. commit) can be completed without going to the server.  If I do not keep a 
	  * copy of the server side history document then the commit operation in particular could be 
	  * much slower.
	  * <p/>
	  * The local history must satisfy the following invariants:
	  * <ol>
	  * <li> the local history starts at revision zero,</li>
	  * <li> the local history ends at some revision at or after the current revision of this document (getRevision()) and
	  * <li> the local history is a subset of the history on the server.
	  * </ol>
	  * 
	  * @return a copy of the changes from revision zero to getRevision().
	  */
	 ChangeDocument getLocalHistory();
	 
	 
	 /**
	  * This call adds to the local copy of the server history when some more of the history of the
	  * document has been retrieved from the server.
	  * <p/>
	  * This call should only be made by callers who know that the invariants of the local history will be
	  * satisfied after the call.
	  * @param changes
	  */
	 void appendLocalHistory(ChangeDocument changes);
	 
	 /**
	  * This call returns changes that have been committed to the server but which are 
	  * not visible on the server until a later revision.
	  * 
	  * @return
	  */
	 ChangeDocument getCommittedChanges();
	 
	 /**
	  * This call allows us to set the committed changes.
	  * 
	  * 
	  * @param commits
	  */
	 void setCommittedChanges(ChangeDocument commits);
	 

	
	OntologyDocumentRevision getRevision();
	
	void setRevision(OntologyDocumentRevision revision);
	
	/**
	 * This call will save the data about the ontology connection, including the server document IRI, the 
	 * local revision and the local history, with alongside the ontology document.
	 * <p/>
	 * The success of this call depends on where the ontology document resides.  If the ontology is stored in a file in a file system
	 * then an implementation of this call could save the ontology connection data in some files in some directory near the ontology file.
	 * However if the ontology is stored somewhere on the web, it is not clear that there will be a way to write this metadata.  But one
	 * could imagine that some future implementation of this method would use web-dav or something similar to save this data.
	 * 
	 * @return true only if it successfully saved the connection data.  It will return false if the location where the 
	 *  ontology is stored is not suitable for saving meta-data associated with the ontology.
	 * @throws IOException
	 */
	boolean saveMetaData() throws IOException;	
}
