package org.protege.owl.server.api;

import java.io.IOException;

import org.semanticweb.owlapi.model.IRI;

/**
 * This class represents an ontology saved in a physical location that has versioning information 
 * associated with it.
 * <p/>
 * A versioned ontology document has two correlated capabilities.  First one can load the ontology into
 * any OWL 2 tool using the local address (e.g. getLocalAddress).  In addition, by some implementation dependent
 * mechanism there is associated information that indicates
 * <ul>
 * <li> the location of the corresponding document on a server.</li>
 * <li> the revision of the last checked out version of the server document</li>
 * <li> a local cache of a subset of the history of the server document</li>
 * </ul>
 * <p/>
 * This provides enough information that many important operations are possible:
 * <ul>
 * <li>One can calculate the uncommitted changes to the ontology document, in some cases without going to the server.  This is done 
 *     by loading the ontology document into an OWL api and comparing the ontology document with the collection of changes made up 
 *     to the document revision.  In some cases the local history cache will contain enough information about the changes that it is unneccessary
 *     to go to the server.</li>
 * <li>One can commit or update local ontology document.
 * </ul>
 * 
 * @author tredmond
 */
public interface VersionedOntologyDocument {

	/**
	 * Get a address for an ontology document that can be loaded into any OWL api.
	 * 
	 * @return a local IRI for the ontology.
	 */
	IRI getLocalAddress();
	
	/**
	 * Gets the local cache of the server history for this document.
	 * 
	 * @return a ChangeDocument representing the full client-side cache of the server side document history.
	 * @throws IOException
	 */
	ChangeDocument getLocalHistory() throws IOException;
	
	/**
	 * A method to add some history (obtained from the server) to the local history cache.
	 * <p/>
	 * This method could have been derived as a combination of other methods.  It is here because in some implementations 
	 * this can be highly optimized.
	 * 
	 * @param changes
	 * @throws IOException
	 */
	void addToLocalHistory(ChangeDocument changes) throws IOException;
	
	
	
	RemoteOntologyDocument getServerDocument() throws IOException;
}
