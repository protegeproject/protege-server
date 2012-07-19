package org.protege.owl.server.impl;

import java.io.File;
import java.io.IOException;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.changes.ChangeDocumentUtilities;
import org.semanticweb.owlapi.model.IRI;


public class VersionedOntologyDocumentImpl implements VersionedOntologyDocument {
	
	public static File getVersioningPropertiesFile(File ontologyFile) {
		File versionInfoDir = getVersionInfoDirectory(ontologyFile);
		return new File(versionInfoDir, ontologyFile.getName() + RemoteOntologyDocumentFromProperties.HISTORY_PROPERTIES_EXTENSION);		
	}

	public static File getHistoryFile(File ontologyFile) {
		File versionInfoDir = getVersionInfoDirectory(ontologyFile);
		return new File(versionInfoDir, ontologyFile.getName() + ChangeDocument.CHANGE_DOCUMENT_EXTENSION);
	}

	public static File getVersionInfoDirectory(File ontologyFile) {
		File dir = ontologyFile.getParentFile();
		return new File(dir, ".owlserver");
	}

	private File localFile;
	private File historyFile;
	private RemoteOntologyDocument serverDoc;
	private ChangeDocument changes;
	
	public VersionedOntologyDocumentImpl(DocumentFactory factory, IRI localAddress) throws IOException {
		locateSources(localAddress);
		serverDoc = RemoteOntologyDocumentFromProperties.read(getVersioningPropertiesFile(localFile));
	}
	
	public VersionedOntologyDocumentImpl(DocumentFactory factory, IRI localAddress, IRI serverAddress, OntologyDocumentRevision revision) 
			throws IOException {
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
			changes = ChangeDocumentUtilities.readChanges(historyFile);
		}
		return changes;
	}

	@Override
	public void addToLocalHistory(ChangeDocument newChanges) throws IOException { /* TODO: Update this when Matthew's stuff comes */
		ChangeDocument originalHistory = getLocalHistory();
		if (newChanges.getStartRevision().compareTo(originalHistory.getEndRevision()) > 0) {
			return;  // don't get angry
		}
		changes = changes.appendChanges(newChanges);
		ChangeDocumentUtilities.writeChanges(changes, historyFile);
	}

	@Override
	public RemoteOntologyDocument getServerDocument() {
		return serverDoc;
	}


}
