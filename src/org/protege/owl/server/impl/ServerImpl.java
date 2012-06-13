package org.protege.owl.server.impl;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.semanticweb.owlapi.model.IRI;

/**
 *  owlserver://hostname.org/path
 * 
 * 
 */

public class ServerImpl implements Server {
	public static final String SCHEME = "owlserver:";
	
	private File root;
	private DocumentFactory factory = new DocumentFactoryImpl();
	
	public ServerImpl(File root) {
		if (!root.isDirectory() || !root.exists()) {
			throw new IllegalStateException("Server does not have a valid root directory");
		}
		this.root = root;
	}

	@Override
	public ServerDocument getServerDocument(User u, IRI serverIRI) {
		sanityCheck(serverIRI);
		URI uri = serverIRI.toURI();
		String path =  serverIRI.toURI().getPath();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		File source = new File(root, path);
		
		throw new IllegalArgumentException();
	}

	
	private void sanityCheck(IRI serverIRI) {
		if (!serverIRI.getScheme().equals(SCHEME)) {
			throw new IllegalStateException("incorrect scheme for server request");
		}
	}

	@Override
	public Collection<ServerDocument> list(User u, ServerDirectory dir) {
		throw new IllegalStateException("Not implemented yet");

	}

	@Override
	public RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI) {
		throw new IllegalStateException("Not implemented yet");

	}

	@Override
	public ServerDirectory createServerDirectory(User u, IRI serverIRI) {
		throw new IllegalStateException("Not implemented yet");

	}

	@Override
	public ChangeDocument getChanges(User u, RemoteOntologyDocument doc,
			OntologyDocumentRevision start, OntologyDocumentRevision end) {
		throw new IllegalStateException("Not implemented yet");

	}

	@Override
	public void applyChange(User u, RemoteOntologyDocument doc,
			String commitComment, OntologyDocumentRevision revision,
			ChangeDocument changes) {
		throw new IllegalStateException("Not implemented yet");

	}


}
