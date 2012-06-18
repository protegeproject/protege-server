package org.protege.owl.server.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.changes.ChangeDocumentImpl;
import org.protege.owl.server.changes.ChangeDocumentUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class VersionedOntologyDocumentImpl implements VersionedOntologyDocument {
	
	public static File getVersioningPropertiesFile(File ontologyFile) {
		File versionInfoDir = getVersionInfoDirectory(ontologyFile);
		return new File(versionInfoDir, ontologyFile.getName() + RemoteOntologyDocumentFromProperties.HISTORY_PROPERTIES_EXTENSION);		
	}

	public static File getHistoryFile(File ontologyFile) {
		File versionInfoDir = getVersionInfoDirectory(ontologyFile);
		return new File(versionInfoDir, ontologyFile.getName() + ChangeDocumentImpl.CHANGE_DOCUMENT_EXTENSION);
	}

	public static File getVersionInfoDirectory(File ontologyFile) {
		File dir = ontologyFile.getParentFile();
		return new File(dir, ".owlserver");
	}

	private DocumentFactory factory;
	private File localFile;
	private File historyFile;
	private RemoteOntologyDocument serverDoc;
	private ChangeDocument changes;
	
	public VersionedOntologyDocumentImpl(DocumentFactory factory, IRI localAddress) throws IOException {
		this.factory = factory;
		locateSources(localAddress);
		serverDoc = RemoteOntologyDocumentFromProperties.read(getVersioningPropertiesFile(localFile));
	}
	
	public VersionedOntologyDocumentImpl(DocumentFactory factory, IRI localAddress, IRI serverAddress, OntologyDocumentRevision revision) 
			throws IOException {
		this.factory = factory;
		locateSources(localAddress);
		serverDoc = RemoteOntologyDocumentFromProperties.create(getVersioningPropertiesFile(localFile), serverAddress, revision);
		ChangeDocumentUtilities.writeEmptyChanges(factory, historyFile);
	}
	
	private void locateSources(IRI localAddress) throws IOException {
		localFile = new File(localAddress.toURI());  // TODO improve exception?
		historyFile = getHistoryFile(localFile);
	}
	
	
	@Override
	public IRI getLocalAddress() {
		return IRI.create(localFile);
	}

	@Override
	public ChangeDocument getLocalHistory() throws IOException {
		if (changes == null) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(historyFile));
			try {
				changes = (ChangeDocument) ois.readObject();
			}
			catch (ClassNotFoundException e) {
				throw new IllegalStateException("Really?!", e);
			}
		}
		return changes;
	}

	@Override
	public void addToLocalHistory(ChangeDocument newChanges) throws IOException { /* TODO: Update this when Matthew's stuff comes */
		ChangeDocument originalHistory = getLocalHistory();
		if (newChanges.getEndRevision().compareTo(originalHistory.getEndRevision()) <= 0) {
			return;
		}
		if (newChanges.getStartRevision().compareTo(originalHistory.getEndRevision()) > 0) {
			return; // don't get angry ;)
		}
		ChangeDocument croppedNewChanges = newChanges.cropChanges(originalHistory.getEndRevision().next(), newChanges.getEndRevision());
		OWLOntology fakeOntology;
		try {
			fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
		}
		catch (OWLOntologyCreationException e) {
			throw new RuntimeException("This really shouldn't happen!", e);
		}
		List<OWLOntologyChange> changeList = new ArrayList<OWLOntologyChange>(originalHistory.getChanges(fakeOntology));
		changeList.addAll(croppedNewChanges.getChanges(fakeOntology));
		Map<OntologyDocumentRevision, String> comments = new TreeMap<OntologyDocumentRevision, String>(originalHistory.getComments());
		comments.putAll(croppedNewChanges.getComments());
		changes = factory.createChangeDocument(changeList, comments, OntologyDocumentRevision.START_REVISION);
		
		ChangeDocumentUtilities.writeChanges(changes, historyFile);
	}

	@Override
	public RemoteOntologyDocument getServerDocument() {
		return serverDoc;
	}


}
