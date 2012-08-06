package org.protege.owl.server.changes;

import java.io.IOException;
import java.util.List;

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

    @Override
	public RemoteOntologyDocument getServerDocument() {
		return vont.getServerDocument();
	}

    @Override
	public ChangeDocument getLocalHistory() {
		return new BufferedChangeDocument(factory, vont.getLocalHistory());
	}

    @Override
	public void appendLocalHistory(ChangeDocument changes) {
		vont.appendLocalHistory(changes);
	}
	
	@Override
	public List<ChangeDocument> getCommittedChanges() {
	    return vont.getCommittedChanges();
	}
	
	@Override
	public void setCommittedChanges(List<ChangeDocument> commits) {
	    vont.setCommittedChanges(commits);
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
