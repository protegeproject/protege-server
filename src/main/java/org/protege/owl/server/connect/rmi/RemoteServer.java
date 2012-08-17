package org.protege.owl.server.connect.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.CommitOption;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.semanticweb.owlapi.model.IRI;

public interface RemoteServer  extends Remote {
	ServerDocument getServerDocument(User u, IRI serverIRI) throws RemoteException;
	
	Collection<ServerDocument> list(User u, ServerDirectory dir) throws RemoteException;
		
	ServerDirectory createDirectory(User u, IRI serverIRI) throws RemoteException;

	RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws RemoteException;
	
	ChangeDocument getChanges(User u, RemoteOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws RemoteException;

	ChangeDocument commit(User u, RemoteOntologyDocument doc, 
	                      ChangeDocument changes, SortedSet<OntologyDocumentRevision> previousCommits,
	                      CommitOption option) throws RemoteException;
		
	void shutdown() throws RemoteException;
}
