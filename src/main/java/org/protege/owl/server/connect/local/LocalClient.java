package org.protege.owl.server.connect.local;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.protege.owl.server.api.exception.DocumentNotFoundException;
import org.protege.owl.server.impl.DocumentFactoryImpl;
import org.protege.owl.server.impl.RemoteOntologyDocumentImpl;
import org.semanticweb.owlapi.model.IRI;

public class LocalClient implements Client {
	private User user;
	private Server server;
	
	public LocalClient(User user, Server server) {
		this.user = user;
		this.server = server;
	}

	@Override
	public DocumentFactory getDocumentFactory() {
		return new DocumentFactoryImpl();
	}

	@Override
	public ServerDocument getServerDocument(IRI serverIRI) throws DocumentNotFoundException {
		return server.getServerDocument(user, serverIRI);
	}

	@Override
	public Collection<ServerDocument> list(ServerDirectory path)
			throws IOException {
		return server.list(user, path);
	}

	@Override
	public ServerDirectory createRemoteDirectory(IRI serverIRI)
			throws IOException {
		return server.createDirectory(user, serverIRI);
	}

	@Override
	public RemoteOntologyDocument createRemoteOntology(IRI serverIRI)
			throws IOException {
		return server.createOntologyDocument(user, serverIRI, new TreeMap<String, Object>());
	}

	@Override
	public ChangeDocument getChanges(RemoteOntologyDocument document,
			OntologyDocumentRevision start, OntologyDocumentRevision end)
			throws IOException {
		return server.getChanges(user, document, start, end);
	}

	@Override
	public void commit(RemoteOntologyDocument document, String commitComment,
					   ChangeDocument changes)
			throws IOException {
		server.commit(user, document, commitComment, changes);
	}

	@Override
	public void shutdown() {
		server.shutdown();
	}

}
