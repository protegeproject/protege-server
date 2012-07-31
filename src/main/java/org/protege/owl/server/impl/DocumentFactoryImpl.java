package org.protege.owl.server.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.protege.owl.server.changes.ChangeDocumentImpl;
import org.protege.owl.server.changes.ChangeDocumentUtilities;
import org.protege.owl.server.changes.OntologyCreationIOException;
import org.protege.owl.server.changes.OntologyToChangesUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DocumentFactoryImpl implements DocumentFactory {
	private Logger logger = Logger.getLogger(DocumentFactoryImpl.class.getCanonicalName());
	
	@SuppressWarnings("deprecation")
	@Override
	public ChangeDocument createChangeDocument(List<OWLOntologyChange> changes,
											   Map<OntologyDocumentRevision, ChangeMetaData> metaData, 
											   OntologyDocumentRevision start) {
		return new ChangeDocumentImpl(this, start, changes, metaData);
	}
	
	@Override
	public VersionedOWLOntology createVersionedOntology(OWLOntology ontology,
													    RemoteOntologyDocument serverDocument,
													    OntologyDocumentRevision revision) {
		ChangeDocument localChanges = emptyChangeDocument();
		return new VersionedOWLOntologyImpl(ontology, serverDocument, revision, localChanges);
	}
	
	@Override
	public boolean hasServerMetadata(OWLOntology ontology) {
		File ontologyFile = VersionedOWLOntologyImpl.getBackingStore(ontology);
		if (ontologyFile == null) {
			return false;
		}
		return VersionedOWLOntologyImpl.getVersioningPropertiesFile(ontologyFile).exists();
	}

	@Override
	public VersionedOWLOntology createVersionedOntology(OWLOntology ontology) throws IOException {
		File ontologyFile = VersionedOWLOntologyImpl.getBackingStore(ontology);
		Properties p = new Properties();
		FileInputStream in = new FileInputStream(VersionedOWLOntologyImpl.getVersioningPropertiesFile(ontologyFile));
		try {
			p.load(in);
		}
		finally {
			in.close();
		}
		OntologyDocumentRevision revision = new OntologyDocumentRevision(Integer.parseInt(p.getProperty(VersionedOWLOntologyImpl.VERSION_PROPERTY)));
		RemoteOntologyDocument serverDocument = new RemoteOntologyDocumentImpl(IRI.create(p.getProperty(VersionedOWLOntologyImpl.BACKING_STORE_PROPERTY)));
		File historyFile = VersionedOWLOntologyImpl.getHistoryFile(ontologyFile);
		ChangeDocument localChanges;
		if (historyFile.exists()) {
			localChanges = ChangeDocumentUtilities.readChanges(this, historyFile, null, null);
		}
		else {
			localChanges = emptyChangeDocument();
		}
		return new VersionedOWLOntologyImpl(ontology, serverDocument, revision, localChanges);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public ChangeDocument readChangeDocument(InputStream in,
											 OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException {
		try {
			ObjectInputStream ois;
			if (in instanceof ObjectInputStream) {
				ois = (ObjectInputStream) in;
			}
			else {
				ois = new ObjectInputStream(in);
			}
			OntologyDocumentRevision startRevision = (OntologyDocumentRevision) ois.readObject();
			@SuppressWarnings("unchecked")
			Map<OntologyDocumentRevision, ChangeMetaData> metaData = (Map<OntologyDocumentRevision, ChangeMetaData>) ois.readObject();
			InputStream is = new BufferedInputStream(ois);
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology changesOntology;
			changesOntology = manager.loadOntologyFromOntologyDocument(is);
			OntologyToChangesUtil otcu = new OntologyToChangesUtil(changesOntology, startRevision);
			otcu.initialise();
			List<OWLOntologyChange> changes = otcu.getChanges();
			return new ChangeDocumentImpl(this, startRevision, changes, metaData);
		}
		catch (OWLOntologyCreationException e) {
			logger.log(Level.WARNING, "Exception caught deserializing change document", e);
			throw new OntologyCreationIOException(e);
		}
		catch (IOException ioe) {
			logger.log(Level.WARNING, "Exception caught deserializing change document", ioe);
			throw ioe;
		}
		catch (ClassNotFoundException cnfe) {
			logger.log(Level.WARNING, "Exception caught deserializing change document", cnfe);
			throw new IOException(cnfe);
		}
		catch (Error e) {
			logger.log(Level.WARNING, "Exception caught deserializing change document", e);
			throw e;
		}
		catch (RuntimeException e) {
			logger.log(Level.WARNING, "Exception caught deserializing change document", e);
			throw e;
		}
	}
	
	@SuppressWarnings("deprecation")
	private ChangeDocument emptyChangeDocument() {
		return new ChangeDocumentImpl(this, OntologyDocumentRevision.START_REVISION, new ArrayList<OWLOntologyChange>(), new TreeMap<OntologyDocumentRevision, ChangeMetaData>());
	}

}
