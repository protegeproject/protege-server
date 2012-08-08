package org.protege.owl.server.changes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.changes.format.OWLOutputStream;
import org.protege.owl.server.util.ChangeUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * 
 * @author tredmond
 */
public class ChangeDocumentImpl implements ChangeDocument, Serializable {
	private static final long serialVersionUID = -3842895051205436375L;
	public static Logger logger = Logger.getLogger(ChangeDocumentImpl.class.getCanonicalName());
	private OntologyDocumentRevision startRevision;
	private List<List<OWLOntologyChange>> changes = new ArrayList<List<OWLOntologyChange>>();
	private SortedMap<OntologyDocumentRevision, ChangeMetaData> metaDataMap = new TreeMap<OntologyDocumentRevision, ChangeMetaData>();
	private DocumentFactory documentFactory;
	
	private ChangeDocumentImpl(OntologyDocumentRevision start, DocumentFactory documentFactory) {
        startRevision = start;
        this.documentFactory = documentFactory;	    
	}
	
	ChangeDocumentImpl(OntologyDocumentRevision start, DocumentFactory documentFactory, List<List<OWLOntologyChange>> changes, SortedMap<OntologyDocumentRevision, ChangeMetaData> metaDataMap) {
	    startRevision = start;
	    this.documentFactory = documentFactory;
	    this.changes = changes;
	    this.metaDataMap = metaDataMap;
	}

	/*
	 * 
	 */
	public ChangeDocumentImpl(DocumentFactory documentFactory, OntologyDocumentRevision startRevision, List<OWLOntologyChange> changes, ChangeMetaData metaData) {
		this.startRevision = startRevision;
		if (changes != null) {
			this.changes.add(new ArrayList<OWLOntologyChange>(changes));
		}
		if (metaData != null && changes != null) {
		    this.metaDataMap.put(startRevision, metaData);
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
	public SortedMap<OntologyDocumentRevision, ChangeMetaData> getMetaData() {
		return Collections.unmodifiableSortedMap(metaDataMap);
	}
	
	@Override
	public ChangeDocument cropChanges(OntologyDocumentRevision start, OntologyDocumentRevision end) {
		if (start == null || start.compareTo(getStartRevision()) < 0) {
			start = getStartRevision();
		}
		if (end == null || end.compareTo(getEndRevision()) > 0) {
			end = getEndRevision();
		}
		if (start.equals(getStartRevision()) && end.equals(getEndRevision())) {
		    return this;
		}
		List<List<OWLOntologyChange>> subChanges = changes.subList(start.getRevision() - startRevision.getRevision(), end.getRevision() - startRevision.getRevision());
		SortedMap<OntologyDocumentRevision, ChangeMetaData> subMetaDataMap = cropMap(metaDataMap, start, end);
		return new ChangeDocumentImpl(start, documentFactory, subChanges, subMetaDataMap);
	}
	
	private <X extends Comparable<X>, Y> SortedMap<X, Y> cropMap(SortedMap<X,Y> map, X start, X end) {
	    if (map.isEmpty()) {
	        return new TreeMap<X,Y>();
	    }
	    if (start.compareTo(map.lastKey()) > 0) {
	        return new TreeMap<X,Y>();
	    }
	    if (end.compareTo(map.firstKey()) < 0) {
	        return new TreeMap<X,Y>();
	    }
	    return map.tailMap(start).headMap(end);
	}
	
	@Override
	public ChangeDocument appendChanges(ChangeDocument additionalChanges) {
		if (additionalChanges.getEndRevision().compareTo(getEndRevision()) <= 0) {
			return this;
		}
		if (additionalChanges.getStartRevision().compareTo(getEndRevision()) > 0) {
			throw new IllegalArgumentException("Changes could not be merged because there was a gap in the change histories");
		}
		OWLOntology fakeOntology;
		try {
			fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
		}
		catch (OWLOntologyCreationException e) {
			throw new RuntimeException("This really shouldn't happen!", e);
		}
		ChangeDocumentImpl newDoc = new ChangeDocumentImpl(startRevision, documentFactory);
		newDoc.changes = new ArrayList<List<OWLOntologyChange>>(changes);
		newDoc.metaDataMap = new TreeMap<OntologyDocumentRevision, ChangeMetaData>(metaDataMap);
		SortedMap<OntologyDocumentRevision, ChangeMetaData> additionalMetaData = additionalChanges.getMetaData();
		for (OntologyDocumentRevision revision = newDoc.getEndRevision();
		        additionalChanges.getEndRevision().compareTo(revision) > 0;
		        revision = revision.next()) {
		    if (additionalMetaData.containsKey(revision)) {
		        newDoc.metaDataMap.put(revision, additionalMetaData.get(revision));
		    }
		    newDoc.changes.add(additionalChanges.cropChanges(revision, revision.next()).getChanges(fakeOntology));
		}
		return newDoc;
	}


	@Override
	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
	    return getChanges(ontology, new TreeSet<OntologyDocumentRevision>());
	}
	
	@Override
	public List<OWLOntologyChange> getChanges(OWLOntology ontology, Set<OntologyDocumentRevision> toIgnore) {
	    List<OWLOntologyChange> filteredChanges = new ArrayList<OWLOntologyChange>();
	    OntologyDocumentRevision revision = startRevision;
	    for (List<OWLOntologyChange> change : changes) {
	        if (!toIgnore.contains(revision)) {
	            filteredChanges.addAll(change);
	        }
	        revision = revision.next();
	    }
	    return ReplaceChangedOntologyVisitor.mutate(ontology, ChangeUtilities.normalizeChangeDelta(filteredChanges));
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
		oos.writeObject(metaDataMap);
		oos.writeInt(changes.size());
		OWLOutputStream owlstream = new OWLOutputStream(oos);
		for (List<OWLOntologyChange> changeSet : changes) {
		    owlstream.write(changeSet);
		}
		oos.flush();
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
	    metaDataMap = doc.metaDataMap;
	}
	
	private void readObjectNoData() throws ObjectStreamException {
		throw new IllegalStateException("huh?");
	}

}
