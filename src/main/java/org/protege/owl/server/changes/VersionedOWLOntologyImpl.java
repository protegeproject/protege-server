package org.protege.owl.server.changes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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
	public static final String HISTORY_PROPERTIES_EXTENSION = ".history-properties";
	
	public static File getVersioningPropertiesFile(File ontologyFile) {
		File versionInfoDir = getVersionInfoDirectory(ontologyFile);
		return new File(versionInfoDir, ontologyFile.getName() + HISTORY_PROPERTIES_EXTENSION);		
	}

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
	
	
	public VersionedOWLOntologyImpl(OWLOntology ontology,
								    RemoteOntologyDocument serverDocument,
								    OntologyDocumentRevision revision,
								    ChangeDocument localHistory) {
		this.ontology = ontology;
		this.serverDocument = serverDocument;
		this.revision = revision;
		this.localHistory = localHistory;
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
		ChangeDocumentUtilities.writeChanges(localHistory, historyFile);
		Properties p = new Properties();
		p.setProperty(VERSION_PROPERTY, new Integer(revision.getRevision()).toString());
		p.setProperty(BACKING_STORE_PROPERTY, serverDocument.getServerLocation().toString());
		FileOutputStream out = new FileOutputStream(getVersioningPropertiesFile(ontologyFile));
		try {
			p.store(out, "OWL Server Properties");
		}
		finally {
			out.close();
		}
		return true;
	}


}
