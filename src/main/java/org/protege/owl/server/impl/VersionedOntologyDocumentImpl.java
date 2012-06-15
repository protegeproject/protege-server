package org.protege.owl.server.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class VersionedOntologyDocumentImpl implements VersionedOntologyDocument {
	public static final String BACKING_STORE_PROPERTY = "server.location";
	public static final String VERSION_PROPERTY       = "version";
	
	public static File getVersioningPropertiesFile(File ontologyFile) {
		File versionInfoDir = getVersionInfoDirectory(ontologyFile);
		return new File(versionInfoDir, ontologyFile.getName() + ".history-properties");		
	}

	public static File getHistoryFile(File ontologyFile) {
		File versionInfoDir = getVersionInfoDirectory(ontologyFile);
		return new File(versionInfoDir, ontologyFile.getName() + ".history");
	}

	public static File getVersionInfoDirectory(File ontologyFile) {
		File dir = ontologyFile.getParentFile();
		return new File(".owlserver");
	}

	private DocumentFactory factory;
	private File localFile;
	private File historyFile;
	private IRI backingStore;
	private OntologyDocumentRevision revision;
	private ChangeDocument changes;
	
	public VersionedOntologyDocumentImpl(DocumentFactory factory, IRI localAddress, IRI backingStore) 
			throws IOException {
		locateSources(localAddress);
	}
	
	private void locateSources(IRI localAddress) throws IOException {
		localFile = new File(localAddress.toURI());  // TODO improve exception?
		historyFile = getHistoryFile(localFile);
		File propertiesFile = getVersioningPropertiesFile(localFile);
		Properties p = new Properties();
		Reader reader = new InputStreamReader(new FileInputStream(propertiesFile), "UTF-8");
		p.load(reader);
		backingStore = IRI.create(p.getProperty(BACKING_STORE_PROPERTY));
		revision = new OntologyDocumentRevision(Integer.parseInt(p.getProperty(VERSION_PROPERTY)));
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
		List<OWLOntologyChange> changeList = new ArrayList(originalHistory.getChanges(fakeOntology));
		changeList.addAll(croppedNewChanges.getChanges(fakeOntology));
		Map<OntologyDocumentRevision, String> comments = new TreeMap<OntologyDocumentRevision, String>(originalHistory.getComments());
		comments.putAll(croppedNewChanges.getComments());
		ChangeDocument combinedChanges = factory.createChangeDocument(changeList, comments, OntologyDocumentRevision.START_REVISION);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(historyFile));
		oos.writeObject(combinedChanges);
		oos.flush();
		oos.close();
	}

	@Override
	public RemoteOntologyDocument getServerDocument() {
		return new RemoteOntologyDocument(backingStore, revision) {
			@Override
			public void setRevision(OntologyDocumentRevision revision) {
				Properties p = new Properties();
				p.setProperty(BACKING_STORE_PROPERTY, backingStore.toString());
				p.setProperty(VERSION_PROPERTY, new Integer(revision.getRevision()).toString());
				File propertiesFile = getVersioningPropertiesFile(localFile);
				try {
					Writer writer = new OutputStreamWriter(new FileOutputStream(propertiesFile), "UTF-8");
					p.store(writer, "Properties saved at " + new Date());
				}
				catch (IOException e) {
					throw new RuntimeException("Unexpected exception writing version properties file for ontology " + localFile);
				}
				super.setRevision(revision);
			}
		};
	}


}
