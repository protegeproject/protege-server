package org.protege.owl.server.connect.local;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.CommitOption;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.DocumentFactoryImpl;
import org.protege.owl.server.util.AbstractClient;
import org.semanticweb.owlapi.model.IRI;

public class LocalClient extends AbstractClient {
	public static final String SCHEME = "local-owl2-server";
	private User user;
	private Server server;
	
	public LocalClient(User user, Server server) {
		this.user = user;
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

	@Override
	public DocumentFactory getDocumentFactory() {
		return new DocumentFactoryImpl();
	}

	@Override
	public ServerDocument getServerDocument(IRI serverIRI) throws OWLServerException {
		return server.getServerDocument(user, serverIRI);
	}

	@Override
	public Collection<ServerDocument> list(ServerDirectory path)
			throws OWLServerException {
		return server.list(user, path);
	}

	@Override
	public ServerDirectory createRemoteDirectory(IRI serverIRI)
			throws OWLServerException {
		return server.createDirectory(user, serverIRI);
	}

	@Override
	public RemoteOntologyDocument createRemoteOntology(IRI serverIRI)
			throws OWLServerException {
		return server.createOntologyDocument(user, serverIRI, new TreeMap<String, Object>());
	}

	@Override
	public ChangeHistory getChanges(RemoteOntologyDocument document,
			OntologyDocumentRevision start, OntologyDocumentRevision end)
			throws OWLServerException {
		return server.getChanges(user, document, start, end);
	}

	@Override
	public ChangeHistory commit(RemoteOntologyDocument document,
					              ChangeHistory changes, SortedSet<OntologyDocumentRevision> previousCommits, CommitOption option)
			throws OWLServerException {
		return server.commit(user, document, changes, previousCommits, option);
	}

	@Override
	public void shutdown() {
		server.shutdown();
	}

}
