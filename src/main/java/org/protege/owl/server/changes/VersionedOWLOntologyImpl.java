package org.protege.owl.server.changes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class VersionedOWLOntologyImpl implements VersionedOWLOntology {
	public static final String BACKING_STORE_PROPERTY = "server.location";
	public static final String VERSION_PROPERTY       = "version";

	public static File getHistoryFile(File ontologyFile) {
		File versionInfoDir = getVersionInfoDirectory(ontologyFile);
		return new File(versionInfoDir, ontologyFile.getName() + ChangeDocument.CHANGE_DOCUMENT_EXTENSION);
	}

	public static File getVersionInfoDirectory(File ontologyFile) {
		File dir = ontologyFile.getParentFile();
		return new File(dir, ".owlserver");
	}
	
    static public File getBackingStore(OWLOntology ontology) {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		IRI documentLocation = manager.getOntologyDocumentIRI(ontology);
		if (documentLocation.getScheme().equals("file")) {
			return null;
		}
		return new File(documentLocation.toURI());
	}
	
	private OWLOntology ontology;
	private RemoteOntologyDocument serverDocument;
	private OntologyDocumentRevision revision;
	private ChangeDocument localHistory;
	private ChangeDocument committedChanges;
	
	
	public VersionedOWLOntologyImpl(OWLOntology ontology,
								    RemoteOntologyDocument serverDocument,
								    OntologyDocumentRevision revision,
								    ChangeDocument localHistory,
								    ChangeDocument committedChanges) {
		this.ontology = ontology;
		this.serverDocument = serverDocument;
		this.revision = revision;
		this.localHistory = localHistory;
		this.committedChanges = committedChanges;
	}

	@Override
	public OWLOntology getOntology() {
		return ontology;
	}


	@Override
	public RemoteOntologyDocument getServerDocument() {
		return serverDocument;
	}
	
	@Override
	public ChangeDocument getLocalHistory() {
		return localHistory;
	}
	
	@Override
	public void appendLocalHistory(ChangeDocument changes) {
		localHistory = localHistory.appendChanges(changes);
	}
	
	@Override
	public ChangeDocument getCommittedChanges() {
	    return committedChanges;
	}
	
	@Override
	public void setCommittedChanges(ChangeDocument commits) {
	    this.committedChanges = commits;
	}
	
	@Override
	public OntologyDocumentRevision getRevision() {
		return revision;
	}
	
	@Override
	public void setRevision(OntologyDocumentRevision revision) {
		this.revision = revision;
	}
	
	@Override
	public boolean saveMetaData() throws IOException {
		File ontologyFile = getBackingStore(ontology);
		if (ontologyFile == null) {
			return false;
		}
		File historyFile = getHistoryFile(ontologyFile);
		historyFile.mkdirs();
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(historyFile)));
		oos.writeObject(revision);
		oos.writeObject(serverDocument);
		oos.writeObject(localHistory);
		oos.writeObject(committedChanges);
		return true;
	}


}
