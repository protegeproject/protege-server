package org.protege.owl.server.connect.rmi;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.CommitOption;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;

public class RemoteServerImpl implements RemoteServer {
	private Server server;
	
	public RemoteServerImpl(Server server) {
		this.server = server;
	}

	@Override
	public ServerDocument getServerDocument(User u, IRI serverIRI) throws RemoteException {
		try {
			return server.getServerDocument(u, serverIRI);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}

	@Override
	public Collection<ServerDocument> list(User u, ServerDirectory dir)
			throws RemoteException {
		try {
			return server.list(u, dir);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}		
	}

	@Override
	public ServerDirectory createDirectory(User u, IRI serverIRI)
			throws RemoteException {
		try {
			return server.createDirectory(u, serverIRI);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}

	@Override
	public RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI,
			Map<String, Object> settings) throws RemoteException {
		try {
			return server.createOntologyDocument(u, serverIRI, settings);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}

	@Override
	public ChangeHistory getChanges(User u, RemoteOntologyDocument doc,
			OntologyDocumentRevision start, OntologyDocumentRevision end)
			throws RemoteException {
		try {
			return server.getChanges(u, doc, start, end);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}

	@Override
	public ChangeHistory commit(User u, RemoteOntologyDocument doc,
			                     ChangeHistory changes, SortedSet<OntologyDocumentRevision> previousCommits, 
			                     CommitOption option)
			throws RemoteException {
		try {
			return server.commit(u, doc, changes, previousCommits, option);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}

	@Override
	public void shutdown() throws RemoteException {
		server.shutdown();
	}

}
