package org.protege.owl.server.changes;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
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
public class ChangeDocumentImpl implements ChangeDocument, Serializable {
	private static final long serialVersionUID = -3842895051205436375L;
	public static Logger logger = Logger.getLogger(ChangeDocumentImpl.class.getCanonicalName());
	private OntologyDocumentRevision startRevision;
	private List<OWLOntologyChange> changes;
	private Map<OntologyDocumentRevision, ChangeMetaData> metaData = new TreeMap<OntologyDocumentRevision, ChangeMetaData>();
	private DocumentFactory documentFactory;

	/*
	 * 
	 */
	public ChangeDocumentImpl(DocumentFactory documentFactory, OntologyDocumentRevision startRevision, List<OWLOntologyChange> changes, Map<OntologyDocumentRevision, ChangeMetaData> metaData) {
		this.startRevision = startRevision;
		if (changes != null) {
			this.changes = new ArrayList<OWLOntologyChange>(changes);
		}
		else {
			this.changes = new ArrayList<OWLOntologyChange>();
		}
		if (metaData != null) {
			this.metaData = new TreeMap<OntologyDocumentRevision, ChangeMetaData>(metaData);
		}
		else {
			this.metaData = new TreeMap<OntologyDocumentRevision, ChangeMetaData>();
		}
		this.documentFactory = documentFactory;
	}

	@Override
	public DocumentFactory getDocumentFactory() {
		return documentFactory;
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
	public Map<OntologyDocumentRevision, ChangeMetaData> getMetaData() {
		return new TreeMap<OntologyDocumentRevision, ChangeMetaData>(metaData);
	}
	
	@Override
	public ChangeDocument cropChanges(OntologyDocumentRevision start, OntologyDocumentRevision end) {
		if (start == null) {
			start = getStartRevision();
		}
		if (end == null) {
			end = getEndRevision();
		}
		if (start.compareTo(getStartRevision()) < 0 || end.compareTo(getEndRevision()) > 0) {
			throw new IllegalStateException("Cropping changes out of range");
		}
		List<OWLOntologyChange> subChanges = changes.subList(start.getRevision() - startRevision.getRevision(), end.getRevision() - startRevision.getRevision());
		Map<OntologyDocumentRevision, ChangeMetaData> newCommitComments = new TreeMap<OntologyDocumentRevision, ChangeMetaData>();
		for (Entry<OntologyDocumentRevision, ChangeMetaData> entry : metaData.entrySet()) {
			OntologyDocumentRevision revision = entry.getKey();
			ChangeMetaData metaDataEntry = entry.getValue();
			if (start.compareTo(revision) <= 0 && revision.compareTo(end) <= 0) {
				newCommitComments.put(revision, metaDataEntry);
			}
		}
		return new ChangeDocumentImpl(documentFactory, start, subChanges, newCommitComments);
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
		Map<OntologyDocumentRevision, ChangeMetaData> comments = new TreeMap<OntologyDocumentRevision, ChangeMetaData>(getMetaData());
		comments.putAll(croppedNewChanges.getMetaData());
		return new ChangeDocumentImpl(documentFactory, getStartRevision(), changeList, comments);
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
					getMetaData().equals(other.getMetaData());
		}
		catch (OWLOntologyCreationException e) {
			throw new IllegalStateException("Could not create empty ontology");
		}
	}
	
	@Override
	public void writeChangeDocument(OutputStream out) throws IOException {
		ObjectOutputStream oos;
		if (out instanceof ObjectOutputStream) {
			oos = (ObjectOutputStream) out;
		}
		else {
			oos = new ObjectOutputStream(out);
		}
		oos.writeObject(startRevision);
		oos.writeObject(metaData);
		OWLOntology changesOntology = ChangesToOntologyVisitor.createChangesOntology(startRevision, changes);
		OWLOntologyManager manager = changesOntology.getOWLOntologyManager();
		Writer writer = new OutputStreamWriter(new BufferedOutputStream(oos), "UTF-8");
		try {
			OWLXMLOntologyFormat format = new OWLXMLOntologyFormat();
			manager.saveOntology(changesOntology, format, new WriterDocumentTarget(writer));
		}
		catch (OWLOntologyStorageException e) {
			throw new OntologyStorageIOException(e);
		}
		finally {
			writer.flush();
		}
	}
	
	@Override
	public String toString() {
		return "{" + startRevision.getRevision() + " --> " + getEndRevision().getRevision() + ": " + changes + "}";
	}
	
	
	

	private void writeObject(ObjectOutputStream out) throws IOException {
	    out.writeObject(getDocumentFactory());
		writeChangeDocument(out);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	    documentFactory = (DocumentFactory) in.readObject();
	    ChangeDocumentImpl doc = (ChangeDocumentImpl) documentFactory.readChangeDocument(in, null, null);
	    startRevision = doc.getStartRevision();
	    changes = doc.changes;
	    metaData = doc.metaData;
	}
	
	private void readObjectNoData() throws ObjectStreamException {
		throw new IllegalStateException("huh?");
	}

}
