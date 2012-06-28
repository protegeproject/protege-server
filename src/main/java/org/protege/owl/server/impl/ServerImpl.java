package org.protege.owl.server.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.CommitWhiteBoard;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.protege.owl.server.api.exception.DocumentAlreadyExistsException;
import org.protege.owl.server.api.exception.DocumentNotFoundException;
import org.protege.owl.server.changes.ChangeDocumentImpl;
import org.protege.owl.server.changes.ChangeDocumentUtilities;
import org.protege.owl.server.util.ChangeUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 *  owlserver://hostname.org/path
 * 
 * 
 */

public class ServerImpl implements Server {
	private Logger logger = Logger.getLogger(Server.class.getCanonicalName());
	
	public enum ServerObjectStatus {
		OBJECT_NOT_FOUND {
			@Override
			public boolean isStatusOf(File f) {
				return !f.exists();
			}
			
		},
		OBJECT_FOUND {
			@Override
			public boolean isStatusOf(File f) {
				return OBJECT_IS_DIRECTORY.isStatusOf(f) || ServerObjectStatus.OBJECT_IS_ONTOLOGY_DOCUMENT.isStatusOf(f);
			}
			
		},
		OBJECT_IS_DIRECTORY {
			@Override
			public boolean isStatusOf(File f) {
				return f.isDirectory();
			}

		},
		OBJECT_IS_ONTOLOGY_DOCUMENT {
			@Override
			public boolean isStatusOf(File f) {
				return f.isFile() && f.getName().endsWith(ChangeDocumentImpl.CHANGE_DOCUMENT_EXTENSION);
			}

		};
		
		public abstract boolean isStatusOf(File f);
	}
	
	public static final String SCHEME = "owlserver";
	
	private File root;
	private DocumentFactory factory = new DocumentFactoryImpl();
	private CommitWhiteBoard commitWhiteBoard;
	
	public ServerImpl(File root) {
		if (!root.isDirectory() || !root.exists()) {
			throw new IllegalStateException("Server does not have a valid root directory");
		}
		this.root = root;
		commitWhiteBoard = new CommitWhiteBoardImpl(this);
	}

	@Override
	public ServerDocument getServerDocument(User u, IRI serverIRI) throws DocumentNotFoundException {
		File f = parseServerIRI(serverIRI, ServerObjectStatus.OBJECT_FOUND);
		if (f == null) {
			throw new DocumentNotFoundException();
		}
		else if (f.isDirectory()) {
			return new ServerDirectoryImpl(serverIRI);
		}
		else {
			return new RemoteOntologyDocumentImpl(serverIRI, OntologyDocumentRevision.START_REVISION);
		}
	}

	
	@Override
	public Collection<ServerDocument> list(User u, ServerDirectory dir) throws DocumentNotFoundException {
		File parent = parseServerIRI(dir.getServerLocation(), ServerObjectStatus.OBJECT_IS_DIRECTORY);
		if (parent == null) {
			throw new IllegalStateException("directory " + dir.getServerLocation() + " does not exist on the server");
		}
		List<ServerDocument> documents = new ArrayList<ServerDocument>();
		for (File child : parent.listFiles()) {
			IRI serverIRI = null;
			try {
				serverIRI = buildServerIRI(child);
			}
			catch (DocumentNotFoundException e) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("File " + child + " does not correspond to a valid server directory or ontology");
				}
				continue;
			}
			if (child.isDirectory()) {
				documents.add(new ServerDirectoryImpl(serverIRI));
			}
			else {
				documents.add(new RemoteOntologyDocumentImpl(serverIRI, OntologyDocumentRevision.START_REVISION));
			}
		}
		return documents;
	}

	@Override
	public RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws IOException {
		File historyFile = parseServerIRI(serverIRI, ServerObjectStatus.OBJECT_NOT_FOUND);
		if (historyFile == null) {
			throw new DocumentAlreadyExistsException("Could not create directory at " + serverIRI);
		}
		if (!historyFile.getName().endsWith(ChangeDocumentImpl.CHANGE_DOCUMENT_EXTENSION)) {
			throw new IllegalArgumentException("Server side IRI's must have the " + ChangeDocumentImpl.CHANGE_DOCUMENT_EXTENSION + " extension");
		}
		ChangeDocumentUtilities.writeEmptyChanges(factory, historyFile);
		return new RemoteOntologyDocumentImpl(serverIRI, OntologyDocumentRevision.START_REVISION);
	}

	@Override
	public ServerDirectory createDirectory(User u, IRI serverIRI) throws IOException  {
		File serverDirectory = parseServerIRI(serverIRI, ServerObjectStatus.OBJECT_NOT_FOUND);
		if (serverDirectory == null) {
			throw new DocumentAlreadyExistsException("Could not create server-side ontology at " + serverIRI);			
		}
		serverDirectory.mkdir();
		return new ServerDirectoryImpl(serverIRI);
	}

	@Override
	public ChangeDocument getChanges(User u, RemoteOntologyDocument doc,
								     OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException {
		File historyFile = parseServerIRI(doc.getServerLocation(), ServerObjectStatus.OBJECT_IS_ONTOLOGY_DOCUMENT);
		if (historyFile == null) {
			throw new IllegalStateException("Expected to find ontology document at the location " + doc.getServerLocation());
		}
		ChangeDocument allChanges = ChangeDocumentUtilities.readChanges(historyFile);
		if (end == null) {
			end = allChanges.getEndRevision();
		}
		return allChanges.cropChanges(start, end);
	}
	


	@Override
	public void commit(User u, RemoteOntologyDocument doc,
					   String commitComment,
					   ChangeDocument changes) throws IOException {
		commitWhiteBoard.init(doc, commitComment, changes);
		OWLOntology fakeOntology;
		try {
			fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
		}
		catch (OWLOntologyCreationException e) {
			throw new IllegalStateException("Why me?");
		}
		List<OWLOntologyChange> serverChanges = commitWhiteBoard.getServerChangesSinceCommit().getChanges(fakeOntology);
		ChangeDocument fullHistory = commitWhiteBoard.getFullChanges();
		
		OntologyDocumentRevision latestRevision = fullHistory.getEndRevision();
		List<OWLOntologyChange> clientChanges = changes.getChanges(fakeOntology);
		List<OWLOntologyChange> changesToCommit = ChangeUtilities.swapOrderOfChangeLists(clientChanges, serverChanges);
		ChangeDocument changeDocumentToAppend = factory.createChangeDocument(changesToCommit, Collections.singletonMap(latestRevision, commitComment), latestRevision);
		ChangeDocument fullHistoryAfterCommit = fullHistory.appendChanges(changeDocumentToAppend);
		ChangeDocumentUtilities.writeChanges(fullHistoryAfterCommit, parseServerIRI(doc.getServerLocation(), ServerObjectStatus.OBJECT_IS_ONTOLOGY_DOCUMENT));
	}
	
	@Override
	public CommitWhiteBoard getCommitWhiteBoard() {
		return commitWhiteBoard;
	}
	
	@Override
	public void shutdown() {

	}

	private File parseServerIRI(IRI serverIRI, ServerObjectStatus expected) throws DocumentNotFoundException {
		if (!serverIRI.getScheme().equals(SCHEME)) {
			throw new IllegalStateException("incorrect scheme for server request");
		}
		String path =  serverIRI.toURI().getPath();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		File f = new File(root, path);
		if (expected.isStatusOf(f)) {
			return f;
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	private IRI buildServerIRI(File f) throws DocumentNotFoundException {
		String path = f.getPath();
		return IRI.create(SCHEME + path);
	}


}
