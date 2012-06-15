package org.protege.owl.server.changes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.ReaderDocumentSource;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * 
 * @author tredmond
 * @deprecated replace with Matthew's binary serialization format.
 */
@Deprecated
public class ChangeDocumentImpl implements ChangeDocument {
	private static final long serialVersionUID = -3842895051205436375L;
	private OntologyDocumentRevision startRevision;
	private List<OWLOntologyChange> changes;

	/*
	 * 
	 */
	public ChangeDocumentImpl(OntologyDocumentRevision startRevision, List<OWLOntologyChange> changes) {
		this.startRevision = startRevision;
		this.changes = changes;
	}

	@Override
	public OntologyDocumentRevision getStartRevision() {
		return startRevision;
	}

	@Override
	public OntologyDocumentRevision getEndRevision() {
		int revision = startRevision.getRevision() + changes.size();
		return new OntologyDocumentRevision(revision);
	}

	@Override
	public Map<OntologyDocumentRevision, String> getComments() {
		throw new IllegalStateException("Not implemented yet");
	}
	
	@Override
	public ChangeDocument cropChanges(OntologyDocumentRevision start, OntologyDocumentRevision end) {
		if (start.compareTo(getStartRevision()) < 0 || end.compareTo(getEndRevision()) > 0) {
			throw new IllegalStateException("Cropping changes out of range");
		}
		List<OWLOntologyChange> subChanges = changes.subList(start.getRevision() - getStartRevision().getRevision(), end.getRevision() - getEndRevision().getRevision());
		return new ChangeDocumentImpl(start, subChanges);
	}

	@Override
	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
		return ReplaceChangedOntologyVisitor.mutate(ontology, changes);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ChangeDocument)) {
			return false;
		}
		ChangeDocument other = (ChangeDocument) o;
		try {
		OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
		return getStartRevision().equals(other.getStartRevision()) &&
				getEndRevision().equals(other.getEndRevision()) &&
				getChanges(ontology).equals(other.getChanges(ontology));
		}
		catch (OWLOntologyCreationException e) {
			throw new IllegalStateException("Could not create empty ontology");
		}
	}
	
	@Override
	public String toString() {
		return "{" + startRevision.getRevision() + " --> " + getEndRevision().getRevision() + ": " + changes + "}";
	}
	

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(startRevision);
		OWLOntology changesOntology = ChangesToOntologyVisitor.createChangesOntology(startRevision, changes);
		OWLOntologyManager manager = changesOntology.getOWLOntologyManager();
		OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
		try {
			OWLXMLOntologyFormat format = new OWLXMLOntologyFormat();
			manager.saveOntology(changesOntology, format, new WriterDocumentTarget(writer));
		}
		catch (OWLOntologyStorageException e) {
			throw new OntologyStorageIOException(e);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		startRevision = (OntologyDocumentRevision) in.readObject();
		InputStreamReader reader = new InputStreamReader(in, "UTF-8");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology changesOntology;
		try {
			changesOntology = manager.loadOntologyFromOntologyDocument(new ReaderDocumentSource(reader));
		}
		catch (OWLOntologyCreationException e) {
			throw new OntologyCreationIOException(e);
		}
		changes = OntologyToChangesUtil.getChanges(changesOntology, startRevision);
	}
	
	private void readObjectNoData() throws ObjectStreamException {
		throw new IllegalStateException("huh?");
	}

}
