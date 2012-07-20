package org.protege.owl.server.changes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
	private Map<OntologyDocumentRevision, String> commitComments = new TreeMap<OntologyDocumentRevision, String>();

	/*
	 * 
	 */
	public ChangeDocumentImpl(OntologyDocumentRevision startRevision, List<OWLOntologyChange> changes, Map<OntologyDocumentRevision, String> commitComments) {
		this.startRevision = startRevision;
		if (changes != null) {
			this.changes = new ArrayList<OWLOntologyChange>(changes);
		}
		else {
			this.changes = new ArrayList<OWLOntologyChange>();
		}
		if (commitComments != null) {
			this.commitComments = new TreeMap<OntologyDocumentRevision, String>(commitComments);
		}
		else {
			this.commitComments = new TreeMap<OntologyDocumentRevision, String>();
		}
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
		return new TreeMap<OntologyDocumentRevision, String>(commitComments);
	}
	
	@Override
	public ChangeDocument cropChanges(OntologyDocumentRevision start, OntologyDocumentRevision end) {
		if (end == null) {
			end = getEndRevision();
		}
		if (start.compareTo(getStartRevision()) < 0 || end.compareTo(getEndRevision()) > 0) {
			throw new IllegalStateException("Cropping changes out of range");
		}
		List<OWLOntologyChange> subChanges = changes.subList(start.getRevision(), end.getRevision());
		Map<OntologyDocumentRevision, String> newCommitComments = new TreeMap<OntologyDocumentRevision, String>();
		for (Entry<OntologyDocumentRevision, String> entry : commitComments.entrySet()) {
			OntologyDocumentRevision revision = entry.getKey();
			String comment = entry.getValue();
			if (start.compareTo(revision) <= 0 && revision.compareTo(end) <= 0) {
				newCommitComments.put(revision, comment);
			}
		}
		return new ChangeDocumentImpl(start, subChanges, newCommitComments);
	}
	
	@Override
	public ChangeDocument appendChanges(ChangeDocument additionalChanges) {
		if (additionalChanges.getEndRevision().compareTo(getEndRevision()) <= 0) {
			return this;
		}
		if (additionalChanges.getStartRevision().compareTo(getEndRevision()) > 0) {
			throw new IllegalArgumentException("Changes could not be merged because there was a gap in the change histories");
		}
		ChangeDocument croppedNewChanges = additionalChanges.cropChanges(getEndRevision(), additionalChanges.getEndRevision());
		OWLOntology fakeOntology;
		try {
			fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
		}
		catch (OWLOntologyCreationException e) {
			throw new RuntimeException("This really shouldn't happen!", e);
		}
		List<OWLOntologyChange> changeList = new ArrayList<OWLOntologyChange>(getChanges(fakeOntology));
		changeList.addAll(croppedNewChanges.getChanges(fakeOntology));
		Map<OntologyDocumentRevision, String> comments = new TreeMap<OntologyDocumentRevision, String>(getComments());
		comments.putAll(croppedNewChanges.getComments());
		return new ChangeDocumentImpl(getStartRevision(), changeList, comments);
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
					getChanges(ontology).equals(other.getChanges(ontology)) &&
					getComments().equals(other.getComments());
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
		OWLOntology changesOntology = ChangesToOntologyVisitor.createChangesOntology(startRevision, changes, commitComments);
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
		OntologyToChangesUtil otcu = new OntologyToChangesUtil(changesOntology, startRevision);
		otcu.initialise();
		changes = otcu.getChanges();
		commitComments = otcu.getCommitComments();
	}
	
	private void readObjectNoData() throws ObjectStreamException {
		throw new IllegalStateException("huh?");
	}

}
