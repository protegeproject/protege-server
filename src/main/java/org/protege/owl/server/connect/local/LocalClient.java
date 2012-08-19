package org.protege.owl.server.connect.local;

import java.util.Collection;
import java.util.TreeMap;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.RemoteServerDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.api.UserId;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.DocumentFactoryImpl;
import org.protege.owl.server.connect.RootUtils;
import org.protege.owl.server.util.AbstractClient;
import org.semanticweb.owlapi.model.IRI;

public class LocalClient extends AbstractClient {
	public static final String SCHEME    = "local-owl2-server";
	public static final String LOCALHOST = "localhost";
	public static final int NO_PORT      = -1;
	private AuthToken authToken;
	private Server server;
	
	public LocalClient(AuthToken user, Server server) {
		this.authToken = user;
		this.server = server;
	}
	
	@Override
	public String getScheme() {
		return SCHEME;
	}
	
	@Override
	public String getAuthority() {
		return "localhost";
	}
	
	public UserId getUserId() {
	    return authToken.getUserId();
	}

	@Override
	public DocumentFactory getDocumentFactory() {
		return new DocumentFactoryImpl();
	}

	@Override
	public RemoteServerDocument getServerDocument(IRI serverIRI) throws OWLServerException {
		return server.getServerDocument(authToken, new ServerPath(serverIRI)).createRemoteDocument(SCHEME, LOCALHOST, NO_PORT);
	}

	@Override
	public Collection<RemoteServerDocument> list(RemoteServerDirectory path)
			throws OWLServerException {
	    return RootUtils.rootList(server.list(authToken, path.createServerDocument()), SCHEME, LOCALHOST, NO_PORT);
	}

	@Override
	public RemoteServerDirectory createRemoteDirectory(IRI serverIRI)
			throws OWLServerException {
		return (RemoteServerDirectory) server.createDirectory(authToken, new ServerPath(serverIRI)).createRemoteDocument(SCHEME, LOCALHOST, NO_PORT);
	}

	@Override
	public RemoteOntologyDocument createRemoteOntology(IRI serverIRI)
			throws OWLServerException {
		return server.createOntologyDocument(authToken, new ServerPath(serverIRI), new TreeMap<String, Object>()).createRemoteDocument(SCHEME, LOCALHOST, NO_PORT);
	}

	@Override
	public ChangeHistory getChanges(RemoteOntologyDocument document,
	                                 OntologyDocumentRevision start, OntologyDocumentRevision end)
			throws OWLServerException {
		return server.getChanges(authToken, document.createServerDocument(), start, end);
	}

	@Override
	public void commit(RemoteOntologyDocument document,
	                    ChangeHistory changes)
			throws OWLServerException {
	    server.commit(authToken, document.createServerDocument(), changes);
	}

}
