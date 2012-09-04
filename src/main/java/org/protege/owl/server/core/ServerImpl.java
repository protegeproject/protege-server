package org.protege.owl.server.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.ServerOntologyDocument;
import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.api.SingletonChangeHistory;
import org.protege.owl.server.api.exception.DocumentAlreadyExistsException;
import org.protege.owl.server.api.exception.DocumentNotFoundException;
import org.protege.owl.server.api.exception.OWLServerException;
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
				        (f.isFile() && f.getName().endsWith(ChangeHistory.CHANGE_DOCUMENT_EXTENSION));
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
	private Collection<ServerTransport> transports = new ArrayList<ServerTransport>();
	
	public ServerImpl(File root, File configurationDir) {
		if (!root.isDirectory() || !root.exists()) {
			throw new IllegalStateException("Server does not have a valid root directory");
		}
		this.root = root;
		this.configurationDir = configurationDir;
		this.pool = new ChangeDocumentPool(factory, 60 * 1000);
	}
	
	@Override
	public OntologyDocumentRevision evaluateRevisionPointer(AuthToken u, ServerOntologyDocument doc, RevisionPointer pointer) throws OWLServerException {
	    switch (pointer.getType()) {
	    case DOCUMENT_REVISION:
	        return pointer.asOntologyDocumentRevision();
	    case HEAD:
	        File historyFile = parseServerIRI(doc.getServerPath(), ServerObjectStatus.OBJECT_IS_ONTOLOGY_DOCUMENT);
	        if (historyFile == null) {
	            throw new IllegalStateException("Expected to find ontology document at the location " + doc.getServerPath());
	        }
	        return pool.getChangeDocument(doc, historyFile).getEndRevision();
	    default:
	        throw new IllegalStateException("Programmer missed a case.");
	    }
	}
	
	@Override
	public ServerDocument getServerDocument(AuthToken u, ServerPath serverPath) throws DocumentNotFoundException {
		File f = parseServerIRI(serverPath, ServerObjectStatus.OBJECT_FOUND);
		if (f == null) {
			throw new DocumentNotFoundException();
		}
		else if (f.isDirectory()) {
			return new ServerDirectoryImpl(serverPath);
		}
		else {
			return new ServerOntologyDocumentImpl(serverPath);
		}
	}

	
	@Override
	public Collection<ServerDocument> list(AuthToken u, ServerDirectory dir) throws DocumentNotFoundException {
		File parent = parseServerIRI(dir.getServerPath(), ServerObjectStatus.OBJECT_IS_DIRECTORY);
		if (parent == null) {
			throw new IllegalStateException("directory " + dir.getServerPath() + " does not exist on the server");
		}
		List<ServerDocument> documents = new ArrayList<ServerDocument>();
		URI rootUri = root.toURI();
		for (File child : parent.listFiles()) {
			ServerPath serverPath = new ServerPath(rootUri.relativize(child.toURI()));
			if (child.isDirectory()) {
				documents.add(new ServerDirectoryImpl(serverPath));
			}
			else {
				documents.add(new ServerOntologyDocumentImpl(serverPath));
			}
		}
		return documents;
	}

	@Override
	public ServerOntologyDocument createOntologyDocument(AuthToken u, ServerPath serverPath, Map<String, Object> settings) throws OWLServerException {
		File historyFile = parseServerIRI(serverPath, ServerObjectStatus.OBJECT_NOT_FOUND);
		if (historyFile == null) {
			throw new DocumentAlreadyExistsException("Could not create directory at " + serverPath);
		}
		if (!historyFile.getName().endsWith(ChangeHistory.CHANGE_DOCUMENT_EXTENSION)) {
			throw new IllegalArgumentException("Server side IRI's must have the " + ChangeHistory.CHANGE_DOCUMENT_EXTENSION + " extension");
		}
		ServerOntologyDocument doc = new ServerOntologyDocumentImpl(serverPath);
		pool.setChangeDocument(doc, historyFile, factory.createEmptyChangeDocument(OntologyDocumentRevision.START_REVISION));
		return doc;
	}

	@Override
	public ServerDirectory createDirectory(AuthToken u, ServerPath serverPath) throws OWLServerException  {
		File serverDirectory = parseServerIRI(serverPath, ServerObjectStatus.OBJECT_NOT_FOUND);
		if (serverDirectory == null) {
			throw new DocumentAlreadyExistsException("Could not create server-side ontology at " + serverPath);			
		}
		serverDirectory.mkdir();
		return new ServerDirectoryImpl(serverPath);
	}

	@Override
	public ChangeHistory getChanges(AuthToken u, ServerOntologyDocument doc,
								     OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException {
		File historyFile = parseServerIRI(doc.getServerPath(), ServerObjectStatus.OBJECT_IS_ONTOLOGY_DOCUMENT);
		if (historyFile == null) {
			throw new IllegalStateException("Expected to find ontology document at the location " + doc.getServerPath());
		}
		return pool.getChangeDocument(doc, historyFile).cropChanges(start, end);
	}
	


	@Override
	public void commit(AuthToken u, ServerOntologyDocument doc,
	                    SingletonChangeHistory changesFromClient) throws OWLServerException {
	    changesFromClient.getMetaData(changesFromClient.getStartRevision()).setUser(u);
	    OntologyDocumentRevision head = evaluateRevisionPointer(u, doc, RevisionPointer.HEAD_REVISION);
		OWLOntology fakeOntology;
		ChangeMetaData metaData = changesFromClient.getMetaData(changesFromClient.getStartRevision());
		try {
			fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
		}
		catch (OWLOntologyCreationException e) {
			throw new IllegalStateException("Why me?", e);
		}
		List<OWLOntologyChange> clientChanges = changesFromClient.getChanges(fakeOntology);
		List<OWLOntologyChange> serverChanges =  adjustServerAndClientChanges(u, 
		                                                                      clientChanges, 
		                                                                      getChanges(u, doc, changesFromClient.getStartRevision(), head), 
		                                                                      fakeOntology);
		ChangeHistory fullHistory = getChanges(u, doc, OntologyDocumentRevision.START_REVISION, head);
		
		OntologyDocumentRevision latestRevision = fullHistory.getEndRevision();
		List<OWLOntologyChange> changesToCommit = ChangeUtilities.swapOrderOfChangeLists(clientChanges, serverChanges);
		ChangeHistory changeDocumentToAppend = factory.createChangeDocument(changesToCommit, metaData, latestRevision);
		ChangeHistory fullHistoryAfterCommit = fullHistory.appendChanges(changeDocumentToAppend);
		pool.setChangeDocument(doc, parseServerIRI(doc.getServerPath(), ServerObjectStatus.OBJECT_IS_ONTOLOGY_DOCUMENT), fullHistoryAfterCommit);
	}
	
	private List<OWLOntologyChange> adjustServerAndClientChanges(AuthToken u, 
	                                                              List<OWLOntologyChange> clientChanges, 
	                                                              ChangeHistory serverChanges, 
	                                                              OWLOntology fakeOntology) {
	    List<OWLOntologyChange> serverChangeList = new ArrayList<OWLOntologyChange>();
	    for (OntologyDocumentRevision revision = serverChanges.getStartRevision();
	            revision.compareTo(serverChanges.getEndRevision()) < 0;
	            revision = revision.next()) {
	        ChangeHistory singleRevisionJump = serverChanges.cropChanges(revision, revision.next());
	        if (singleRevisionJump.getMetaData(revision).getUserId().equals(u.getUserId())) {
	            // this is somewhat cosmetic but we don't want to see the same commit twice
	            clientChanges.removeAll(singleRevisionJump.getChanges(fakeOntology));
	        }
	        else {
	            // only add changes from other users - we don't want previous changes from our user to block new changes from our user.
	            serverChangeList.addAll(singleRevisionJump.getChanges(fakeOntology));
	        }
	    }    
	    return serverChangeList;
	}

	@Override
	public void shutdown(AuthToken u) {
	    shutdown();
	}
	
	@Override
	public void shutdown() {
	    pool.dispose();
	}


	private File parseServerIRI(ServerPath path, ServerObjectStatus expected) throws DocumentNotFoundException {
		File f = new File(root, path.pathAsString());
		boolean pooledDocumentFound = pool.testServerLocation(path);
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

	/* Interfaces that are not visible to the client. */

	@Override
	public InputStream getConfigurationInputStream(String fileName) throws OWLServerException {
	    try {
	        return new FileInputStream(new File(configurationDir, fileName));
	    }
	    catch (FileNotFoundException fnfe) {
	        throw new DocumentNotFoundException(fnfe);
	    }
	}

	@Override
	public OutputStream getConfigurationOutputStream(String fileName) throws OWLServerException {
	    try {
	        return new FileOutputStream(new File(configurationDir, fileName));
	    }
	    catch (FileNotFoundException fnfe) {
	        throw new DocumentNotFoundException(fnfe);
	    }
	}
	
    @Override
    public InputStream getConfigurationInputStream(ServerDocument doc, String extension) throws OWLServerException {
        try {
            return new FileInputStream(getConfiguration(doc, extension));
        }
        catch (FileNotFoundException fnfe) {
            throw new DocumentNotFoundException(fnfe);
        }
    }
    
    @Override
    public OutputStream getConfigurationOutputStream(ServerDocument doc, String extension) throws OWLServerException {
        try {
            return new FileOutputStream(getConfiguration(doc, extension));
        }
        catch (FileNotFoundException fnfe) {
            throw new DocumentNotFoundException(fnfe);
        }
    }
	
	private File getConfiguration(ServerDocument doc, String extension) throws DocumentNotFoundException {
	    File f = parseServerIRI(doc.getServerPath(), ServerObjectStatus.ANY);
	    if (doc instanceof ServerDirectory) {
	        return new File(f, "-" + extension);
	    }
	    String fullName = f.getPath();
	    return new File(fullName + "-" + extension);
	}

	@Override
	public void setTransports(Collection<ServerTransport> transports) {
	    this.transports.clear();
	    this.transports.addAll(transports);
	}

	@Override
	public Collection<ServerTransport> getTransports() {
	    return Collections.unmodifiableCollection(transports);
	}
	
	@Override
	public DocumentFactory getDocumentFactory() {
	    return factory;
	}
}
