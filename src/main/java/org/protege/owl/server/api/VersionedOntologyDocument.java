package org.protege.owl.server.api;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This is an open OWL ontology held by the client with an association to a corresponding server
 * document at a particular revision.  
 * <p/>
 * This is a central class but it is more a container of the required data than a set of required methods for 
 * manipulating a versioned ontology document.  Devloper who want to manipulate this class should look at the 
 * ClientUtilities class which contains utilities that 
 * <ol>
 * <li>load a versioned ontology document from a server,</li>
 * <li>load or save a versioned ontology document from disk and</li>
 * <li>commit or update a versioned ontology document by interacting with a server</li>
 * </ol>
 * 
 * @author redmond
 *
 */
public interface VersionedOntologyDocument {

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
	 ChangeHistory getLocalHistory();
	 
	 
	 /**
	  * This call adds to the local copy of the server history when some more of the history of the
	  * document has been retrieved from the server.
	  * <p/>
	  * This call should only be made by callers who know that the invariants of the local history will be
	  * satisfied after the call.
	  * @param changes
	  */
    void appendLocalHistory(ChangeHistory changes);

		
	OntologyDocumentRevision getRevision();
	
	void setRevision(OntologyDocumentRevision revision);
	
	/**
	 * This call will save the data about the ontology connection, including the server document IRI, the 
	 * local revision and the local history.
	 * <p/>
	 * The success of this call depends on where the ontology document resides.  If the ontology is stored in a file in a file system
	 * then an implementation of this call could save the ontology connection data in some files in some directory near the ontology file.
	 * However if the ontology is stored somewhere on the web, it is not clear that there will be a way to write this metadata.  But one
	 * could imagine that some future implementation of this method would use web-dav or something similar to save this data.
	 * <p/>
	 * This routine will also save the history data as is done by the saveLocalHistory() function call.
	 * 
	 * 
	 * @return true only if it successfully saved the connection data.  It will return false if the location where the 
	 *  ontology is stored is not suitable for saving meta-data associated with the ontology.
	 * @throws IOException
	 */
	boolean saveMetaData() throws IOException;
	
	/**
	 * This call will save the local history associated with a versioned ontology document.
	 * <p/>
	 * The success of this call depends on where the ontology document resides exactly as described in the 
	 * documentation of the saveMetaData() call.  The reason that this call isn't simply subsumed in the 
	 * saveMetaData call is that there are occasions where the local history can be saved more aggressively 
	 * than the meta data can.  For example, an ontology editor should probably not save the metadata associated with 
	 * an ontology until the ontology is saved.  That is the ontology editor is responsible for keeping the metadata 
	 * synchronized with the copy of the ontology on disk.  However the change history can be saved anytime it is updated
	 * because the change history for an ontology document can not change structurally, it can only be appended to.
	 * 
	 * @return
	 * @throws IOException
	 */
	boolean saveLocalHistory() throws IOException;
}
