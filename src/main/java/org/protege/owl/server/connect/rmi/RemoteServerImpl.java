package org.protege.owl.server.connect.rmi;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.ServerOntologyDocument;
import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.api.SingletonChangeHistory;
import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;

public class RemoteServerImpl implements RemoteServer {
	private Server server;
	
	public RemoteServerImpl(Server server) {
		this.server = server;
	}
	
	@Override
	public OntologyDocumentRevision evaluateRevisionPointer(AuthToken u, ServerOntologyDocument doc, RevisionPointer pointer) throws RemoteException {
	       try {
	            return server.evaluateRevisionPointer(u, doc, pointer);
	        }
	        catch (OWLServerException ioe) {
	            throw new RemoteException(ioe.getMessage(), ioe);
	        }
	}

	@Override
	public ServerDocument getServerDocument(AuthToken u, IRI serverIRI) throws RemoteException {
		try {
			return server.getServerDocument(u, new ServerPath(serverIRI));
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}

	@Override
	public Collection<ServerDocument> list(AuthToken u, ServerDirectory dir)
			throws RemoteException {
		try {
			return server.list(u, dir);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}		
	}

	@Override
	public ServerDirectory createDirectory(AuthToken u, IRI serverIRI)
			throws RemoteException {
		try {
			return server.createDirectory(u, new ServerPath(serverIRI));
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}

	@Override
	public ServerOntologyDocument createOntologyDocument(AuthToken u, IRI serverIRI,
			Map<String, Object> settings) throws RemoteException {
		try {
			return server.createOntologyDocument(u, new ServerPath(serverIRI), settings);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}

	@Override
	public ChangeHistory getChanges(AuthToken u, ServerOntologyDocument doc,
	                                RevisionPointer startPointer, RevisionPointer endPointer)
			throws RemoteException {
		try {
		    OntologyDocumentRevision start = server.evaluateRevisionPointer(u, doc, startPointer);
		    OntologyDocumentRevision end   = server.evaluateRevisionPointer(u, doc, endPointer);
			return server.getChanges(u, doc, start, end);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}

	@Override
	public void commit(AuthToken u, ServerOntologyDocument doc,
	                    SingletonChangeHistory changes)
			throws RemoteException {
		try {
		    server.commit(u, doc, changes);
		}
		catch (OWLServerException ioe) {
			throw new RemoteException(ioe.getMessage(), ioe);
		}
	}
	
	@Override
	public void shutdown(AuthToken u) throws RemoteException {
	    try {
	        server.shutdown(u);
	    }
	    catch (OWLServerException ioe) {
	        throw new RemoteException(ioe.getMessage(), ioe);
	    }
	}

}
