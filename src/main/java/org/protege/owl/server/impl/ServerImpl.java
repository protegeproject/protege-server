package org.protege.owl.server.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.DocumentNotFoundException;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.protege.owl.server.changes.ChangeDocumentImpl;
import org.protege.owl.server.changes.ChangeDocumentUtilities;
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
	public ServerDocument getServerDocument(User u, IRI serverIRI) throws DocumentNotFoundException {
		File f = parseServerIRI(serverIRI);
		if (f.isDirectory()) {
			return new ServerDirectory(serverIRI);
		}
		else {
			return new RemoteOntologyDocument(serverIRI, OntologyDocumentRevision.START_REVISION);
		}
	}

	
	private File parseServerIRI(IRI serverIRI) throws DocumentNotFoundException {
		if (!serverIRI.getScheme().equals(SCHEME)) {
			throw new IllegalStateException("incorrect scheme for server request");
		}
		URI uri = serverIRI.toURI();
		String path =  serverIRI.toURI().getPath();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		File sourceDir = new File(root, path);
		if (sourceDir.exists()) {
			return sourceDir;
		}
		File historyFile = new File(root, path + ChangeDocumentImpl.CHANGE_DOCUMENT_EXTENSION);
		if (historyFile.exists()) {
			return historyFile;
		}
		throw new DocumentNotFoundException("Document not found on server at location " + serverIRI);
	}
	
	@SuppressWarnings("deprecation")
	private IRI buildServerIRI(File f) throws DocumentNotFoundException {
		String path = f.getPath();
		if (f.isDirectory()) {
			return IRI.create(SCHEME + path);
		}
		else if (path.endsWith(ChangeDocumentImpl.CHANGE_DOCUMENT_EXTENSION)){
			String path2 = path.substring(0, path.length() - ChangeDocumentImpl.CHANGE_DOCUMENT_EXTENSION.length());
			return IRI.create(SCHEME + path2);
		}
		throw new DocumentNotFoundException("Document not found on server at location " + path);
	}

	@Override
	public Collection<ServerDocument> list(User u, ServerDirectory dir) throws DocumentNotFoundException {
		File parent = parseServerIRI(dir.getServerLocation());
		List<ServerDocument> documents = new ArrayList<ServerDocument>();
		for (File child : parent.listFiles()) {
			IRI serverIRI = buildServerIRI(child);
			if (child.isDirectory()) {
				documents.add(new ServerDirectory(serverIRI));
			}
			else {
				documents.add(new RemoteOntologyDocument(serverIRI, OntologyDocumentRevision.START_REVISION));
			}
		}
		return documents;
	}

	@Override
	public RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws IOException {
		File historyFile = parseServerIRI(serverIRI);
		ChangeDocumentUtilities.writeEmptyChanges(factory, historyFile);
		return new RemoteOntologyDocument(serverIRI, OntologyDocumentRevision.START_REVISION);
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
