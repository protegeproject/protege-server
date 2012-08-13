package org.protege.owl.server.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.User;
import org.protege.owl.server.api.exception.DocumentAlreadyExistsException;
import org.protege.owl.server.api.exception.DocumentNotFoundException;
import org.protege.owl.server.changes.ChangeDocumentUtilities;
import org.protege.owl.server.changes.DocumentFactoryImpl;
import org.protege.owl.server.util.ChangeUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


/*
 * ToDo Waiting for Matthew's change document work to be committed.  It is probably relevant in either case that 
 * the format of the saved changes document does not have to be identical to the serialization of the object. 
 * Using a different format and perhaps using multiple files will allow us to optimize - particularly the commit and getChanges calls.
 */


/**
 *  owlserver://hostname.org/path
 * 
 * 
 */

public class ServerImpl implements Server {
	
	public enum ServerObjectStatus {
		OBJECT_NOT_FOUND {
			@Override
			public boolean isStatusOf(File f, boolean pooledDocumentFound) {
				return !pooledDocumentFound && !f.exists();
			}
			
		},
		OBJECT_FOUND {
			@Override
			public boolean isStatusOf(File f, boolean pooledDocumentFound) {
				return pooledDocumentFound || 
				        OBJECT_IS_DIRECTORY.isStatusOf(f, pooledDocumentFound) || 
				        ServerObjectStatus.OBJECT_IS_ONTOLOGY_DOCUMENT.isStatusOf(f, pooledDocumentFound);
			}
			
		},
		OBJECT_IS_DIRECTORY {
			@Override
			public boolean isStatusOf(File f, boolean pooledDocumentFound) {
				return f.isDirectory();
			}

		},
		OBJECT_IS_ONTOLOGY_DOCUMENT {
			@Override
			public boolean isStatusOf(File f, boolean pooledDocumentFound) {
				return pooledDocumentFound ||
				        (f.isFile() && f.getName().endsWith(ChangeDocument.CHANGE_DOCUMENT_EXTENSION));
			}

		},
		
		ANY {
		    @Override
		    public boolean isStatusOf(File f, boolean pooledDocumentFound) {
		        return true;
		    }
		};
		
		public abstract boolean isStatusOf(File f, boolean pooledDocumentFound);
	}
	
	private File root;
	private File configurationDir;
	private DocumentFactory factory = new DocumentFactoryImpl();
	private ChangeDocumentPool pool;
	
	public ServerImpl(File root, File configurationDir) {
		if (!root.isDirectory() || !root.exists()) {
			throw new IllegalStateException("Server does not have a valid root directory");
		}
		this.root = root;
		this.configurationDir = configurationDir;
		this.pool = new ChangeDocumentPool(factory, 15 * 60 * 1000);
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
			return new RemoteOntologyDocumentImpl(serverIRI);
		}
	}

	
	@Override
	public Collection<ServerDocument> list(User u, ServerDirectory dir) throws DocumentNotFoundException {
		File parent = parseServerIRI(dir.getServerLocation(), ServerObjectStatus.OBJECT_IS_DIRECTORY);
		if (parent == null) {
			throw new IllegalStateException("directory " + dir.getServerLocation() + " does not exist on the server");
		}
		List<ServerDocument> documents = new ArrayList<ServerDocument>();
		URI rootUri = root.toURI();
		for (File child : parent.listFiles()) {
			String relativeChildPath = rootUri.relativize(child.toURI()).getPath();
			IRI serverIRI = createIRI(dir.getServerLocation(), relativeChildPath);
			if (child.isDirectory()) {
				documents.add(new ServerDirectoryImpl(serverIRI));
			}
			else {
				documents.add(new RemoteOntologyDocumentImpl(serverIRI));
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
		if (!historyFile.getName().endsWith(ChangeDocument.CHANGE_DOCUMENT_EXTENSION)) {
			throw new IllegalArgumentException("Server side IRI's must have the " + ChangeDocument.CHANGE_DOCUMENT_EXTENSION + " extension");
		}
		RemoteOntologyDocument doc = new RemoteOntologyDocumentImpl(serverIRI);
		pool.setChangeDocument(doc, historyFile, factory.createEmptyChangeDocument(OntologyDocumentRevision.START_REVISION));
		return doc;
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
		return pool.getChangeDocument(doc, historyFile).cropChanges(start, end);
	}
	


	@Override
	public ChangeDocument commit(User u, RemoteOntologyDocument doc,
	                             ChangeMetaData metaData,
	                             ChangeDocument changes, 
	                             SortedSet<OntologyDocumentRevision> previousCommits) throws IOException {
		OWLOntology fakeOntology;
		try {
			fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
		}
		catch (OWLOntologyCreationException e) {
			throw new IllegalStateException("Why me?", e);
		}
		List<OWLOntologyChange> serverChanges = getChanges(u, doc, changes.getStartRevision(), null).getChanges(fakeOntology, previousCommits);
		ChangeDocument fullHistory = getChanges(u, doc, OntologyDocumentRevision.START_REVISION, null);
		
		OntologyDocumentRevision latestRevision = fullHistory.getEndRevision();
		List<OWLOntologyChange> clientChanges = changes.getChanges(fakeOntology);
		List<OWLOntologyChange> changesToCommit = ChangeUtilities.swapOrderOfChangeLists(clientChanges, serverChanges);
		ChangeDocument changeDocumentToAppend = factory.createChangeDocument(changesToCommit, metaData, latestRevision);
		ChangeDocument fullHistoryAfterCommit = fullHistory.appendChanges(changeDocumentToAppend);
		pool.setChangeDocument(doc, parseServerIRI(doc.getServerLocation(), ServerObjectStatus.OBJECT_IS_ONTOLOGY_DOCUMENT), fullHistoryAfterCommit);
		return factory.createChangeDocument(changesToCommit, new ChangeMetaData(), fullHistory.getEndRevision());
	}

	@Override
	public void shutdown() {
	    pool.dispose();
	}
	
	@Override
	public File getConfiguration(String fileName) {
	    return new File(configurationDir, fileName);
	}
	
	@Override
	public File getConfiguration(ServerDocument doc, String extension) throws DocumentNotFoundException {
	    File f = parseServerIRI(doc.getServerLocation(), ServerObjectStatus.ANY);
	    String fullName = f.getPath();
	    String prefix;
	    if (fullName.endsWith(ChangeDocument.CHANGE_DOCUMENT_EXTENSION)) {
	        prefix = fullName.substring(0, fullName.length() - ChangeDocument.CHANGE_DOCUMENT_EXTENSION.length());
	    }
	    else {
	        prefix = fullName;
	    }
	    return new File(prefix + extension);
	}

	private File parseServerIRI(IRI serverIRI, ServerObjectStatus expected) throws DocumentNotFoundException {
		String path =  serverIRI.toURI().getPath();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		File f = new File(root, path);
		boolean pooledDocumentFound = pool.testServerLocation(serverIRI);
		if (expected.isStatusOf(f, pooledDocumentFound)) {
			return f;
		}
		return null;
	}
	
	private IRI createIRI(IRI model, String path) {
		URI modelUri = model.toURI();
		StringBuffer iriBuffer = new StringBuffer();
		iriBuffer.append(model.getScheme());
		iriBuffer.append("://");
		iriBuffer.append(modelUri.getAuthority());
		if (!path.startsWith("/")) {
			iriBuffer.append('/');
		}
		iriBuffer.append(path);
		return IRI.create(iriBuffer.toString());
	}


}
