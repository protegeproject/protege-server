package org.protege.owl.server.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.protege.owl.server.changes.ChangeDocumentImpl;
import org.protege.owl.server.changes.ChangeDocumentUtilities;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class DocumentFactoryImpl implements DocumentFactory {

	@SuppressWarnings("deprecation")
	@Override
	public ChangeDocument createChangeDocument(List<OWLOntologyChange> changes,
											   Map<OntologyDocumentRevision, String> commitComments, 
											   OntologyDocumentRevision start) {
		return new ChangeDocumentImpl(start, changes, commitComments);
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
			localChanges = ChangeDocumentUtilities.readChanges(historyFile);
		}
		else {
			localChanges = emptyChangeDocument();
		}
		return new VersionedOWLOntologyImpl(ontology, serverDocument, revision, localChanges);
	}
	
	@SuppressWarnings("deprecation")
	private ChangeDocument emptyChangeDocument() {
		return new ChangeDocumentImpl(OntologyDocumentRevision.START_REVISION, new ArrayList<OWLOntologyChange>(), new TreeMap<OntologyDocumentRevision, String>());
	}

}
