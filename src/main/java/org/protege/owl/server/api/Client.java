package org.protege.owl.server.api;

import java.util.Collection;

import org.semanticweb.owlapi.model.IRI;

public interface Client {
	
	/**
	 * This call gets the document factory.  This factory defines how the Client implements ChangeDocuments and
	 * how the client retrieves information about a remote server associated with an ontology stored on a local disk.
	 * 
	 * @return
	 */
	DocumentFactory getDocumentFactory();
	
	/**
	 * If you pass an address for an object on the server, the server will try to return the
	 * ServerDocument associated with that address.
	 * <p/>
	 * The ServerDocument might be either a RemoteOntologyDocument or a ServerDirectory.
	 * 
	 * @param serverIRI
	 * @return
	 */
	ServerDocument getServerDocument(IRI serverIRI) throws DocumentNotFoundException;
	
	
	/**
	 * Returns a list of the Server documents in a server directory
	 * 
	 * @param path
	 * @return
	 */
	Collection<ServerDocument> list(ServerDirectory path);
	
	/**
	 * Allows the user to create a directory on the server.
	 * 
	 * @param serverIRI
	 */
	ServerDirectory createOntologyDirectory(IRI serverIRI);
	
	/**
	 * Allows the user to create an empty ontology document on the server.
	 * <p/>
	 * A typical pattern will be for the user to create a remote ontology document with this call and 
	 * then commit a collection of changes filling in the remote content with the commit call.
	 * 
	 * @param commitComment
	 * @param serverIRI
	 * @return
	 */
	RemoteOntologyDocument createRemoteOntologyDocument(IRI serverIRI);
		
	/**
	 * Gets the head revision of the Remote Ontology Document.
	 * 
	 * @param doc
	 * @return
	 */
	OntologyDocumentRevision getHeadRevision(RemoteOntologyDocument doc);
	
	/**
	 * Retrieves the list of changes for the RemoteOntology Document from a given start revision
	 * to a given end revision.  
	 * 
	 * @param document
	 * @param start
	 * @param end
	 * @return
	 */
	ChangeDocument getChanges(RemoteOntologyDocument document, OntologyDocumentRevision start, OntologyDocumentRevision end);
	
	/**
	 * Commits a collection of changes to the remote ontology document.
	 * 
	 * @param document
	 * @param revision
	 * @param changes
	 */
	void commit(RemoteOntologyDocument document, OntologyDocumentRevision revision, ChangeDocument changes);

}
