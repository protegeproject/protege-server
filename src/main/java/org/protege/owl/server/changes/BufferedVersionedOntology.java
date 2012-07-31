package org.protege.owl.server.changes;

import java.io.IOException;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.semanticweb.owlapi.model.OWLOntology;

public class BufferedVersionedOntology implements VersionedOWLOntology {
	private VersionedOWLOntology vont;
	private BufferedDocumentFactory factory;
	
	public BufferedVersionedOntology(BufferedDocumentFactory factory, VersionedOWLOntology versionedOWLOntology) {
		vont = versionedOWLOntology;
		this.factory = factory;
	}

	public OWLOntology getOntology() {
		return vont.getOntology();
	}

	public RemoteOntologyDocument getServerDocument() {
		return vont.getServerDocument();
	}

	public ChangeDocument getLocalHistory() {
		return new BufferedChangeDocument(factory, vont.getLocalHistory());
	}

	public void appendLocalHistory(ChangeDocument changes) {
		vont.appendLocalHistory(changes);
	}

	public OntologyDocumentRevision getRevision() {
		return vont.getRevision();
	}

	public void setRevision(OntologyDocumentRevision revision) {
		vont.setRevision(revision);
	}

	public boolean saveMetaData() throws IOException {
		return vont.saveMetaData();
	}
	
	
}
