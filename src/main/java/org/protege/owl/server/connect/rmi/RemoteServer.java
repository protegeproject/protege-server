package org.protege.owl.server.connect.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.ServerOntologyDocument;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.AuthToken;
import org.semanticweb.owlapi.model.IRI;

public interface RemoteServer  extends Remote {
	ServerDocument getServerDocument(AuthToken u, IRI serverIRI) throws RemoteException;
	
	Collection<ServerDocument> list(AuthToken u, ServerDirectory dir) throws RemoteException;
		
	ServerDirectory createDirectory(AuthToken u, IRI serverIRI) throws RemoteException;

	ServerOntologyDocument createOntologyDocument(AuthToken u, IRI serverIRI, Map<String, Object> settings) throws RemoteException;
	
	ChangeHistory getChanges(AuthToken u, ServerOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws RemoteException;

	void commit(AuthToken u, ServerOntologyDocument doc, 
	             ChangeHistory changes) throws RemoteException;
		
}
