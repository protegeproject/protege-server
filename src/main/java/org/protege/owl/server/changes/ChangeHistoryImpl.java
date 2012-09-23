package org.protege.owl.server.changes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeHistory;
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
public class ChangeHistoryImpl implements ChangeHistory, Serializable {
	private static final long serialVersionUID = -3842895051205436375L;
	public static Logger logger = Logger.getLogger(ChangeHistoryImpl.class.getCanonicalName());
	private OntologyDocumentRevision startRevision;
	private List<List<OWLOntologyChange>> listOfRevisionChanges = new ArrayList<List<OWLOntologyChange>>();
	private SortedMap<OntologyDocumentRevision, ChangeMetaData> metaDataMap = new TreeMap<OntologyDocumentRevision, ChangeMetaData>();
	private DocumentFactory documentFactory;
	
	private ChangeHistoryImpl(OntologyDocumentRevision start, DocumentFactory documentFactory) {
        startRevision = start;
        this.documentFactory = documentFactory;	    
	}
	
	ChangeHistoryImpl(OntologyDocumentRevision start, DocumentFactory documentFactory, List<List<OWLOntologyChange>> listOfRevisionChanges, SortedMap<OntologyDocumentRevision, ChangeMetaData> metaDataMap) {
	    startRevision = start;
	    this.documentFactory = documentFactory;
	    this.listOfRevisionChanges = listOfRevisionChanges;
	    this.metaDataMap = metaDataMap;
	}

	/*
	 * 
	 */
	public ChangeHistoryImpl(DocumentFactory documentFactory, OntologyDocumentRevision startRevision, List<OWLOntologyChange> changes, ChangeMetaData metaData) {
		if (metaData == null) {
		    metaData = new ChangeMetaData();
		}
	    this.startRevision = startRevision;
		if (changes != null) {
			this.listOfRevisionChanges.add(new ArrayList<OWLOntologyChange>(changes));
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
	    return startRevision.add(listOfRevisionChanges.size());
	}

	@Override
	public ChangeMetaData getMetaData(OntologyDocumentRevision revision) {
		return metaDataMap.get(revision);
	}
	
	@Override
	public ChangeHistory cropChanges(OntologyDocumentRevision start, OntologyDocumentRevision end) {
		if (start == null || start.compareTo(getStartRevision()) < 0) {
			start = getStartRevision();
		}
		if (end == null || end.compareTo(getEndRevision()) > 0) {
			end = getEndRevision();
		}
		if (start.equals(getStartRevision()) && end.equals(getEndRevision())) {
		    return this;
		}
		List<List<OWLOntologyChange>> subChanges = listOfRevisionChanges.subList(start.getRevisionDifferenceFrom(startRevision), end.getRevisionDifferenceFrom(startRevision));
		SortedMap<OntologyDocumentRevision, ChangeMetaData> subMetaDataMap = cropMap(metaDataMap, start, end);
		return new ChangeHistoryImpl(start, documentFactory, subChanges, subMetaDataMap);
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
	public ChangeHistory appendChanges(ChangeHistory additionalChanges) {
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
		ChangeHistoryImpl newDoc = new ChangeHistoryImpl(startRevision, documentFactory);
		newDoc.listOfRevisionChanges = new ArrayList<List<OWLOntologyChange>>(listOfRevisionChanges);
		newDoc.metaDataMap = new TreeMap<OntologyDocumentRevision, ChangeMetaData>(metaDataMap);
		for (OntologyDocumentRevision revision = newDoc.getEndRevision();
		        additionalChanges.getEndRevision().compareTo(revision) > 0;
		        revision = revision.next()) {
		    newDoc.metaDataMap.put(revision, additionalChanges.getMetaData(revision));
		    newDoc.listOfRevisionChanges.add(additionalChanges.cropChanges(revision, revision.next()).getChanges(fakeOntology));
		}
		return newDoc;
	}


	@Override
	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
	    List<OWLOntologyChange> filteredChanges = new ArrayList<OWLOntologyChange>();
	    OntologyDocumentRevision revision = startRevision;
	    for (List<OWLOntologyChange> change : listOfRevisionChanges) {
	        filteredChanges.addAll(change);
	        revision = revision.next();
	    }
	    return ReplaceChangedOntologyVisitor.mutate(ontology, ChangeUtilities.normalizeChangeDelta(filteredChanges));
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
		oos.writeInt(listOfRevisionChanges.size());
		OWLOutputStream owlstream = new OWLOutputStream(oos);
		for (List<OWLOntologyChange> changeSet : listOfRevisionChanges) {
		    owlstream.write(changeSet);
		}
		oos.flush();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
	    out.writeObject(getDocumentFactory());
		writeChangeDocument(out);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	    documentFactory = (DocumentFactory) in.readObject();
	    ChangeHistoryImpl doc = (ChangeHistoryImpl) documentFactory.readChangeDocument(in, null, null);
	    startRevision = doc.getStartRevision();
	    listOfRevisionChanges = doc.listOfRevisionChanges;
	    metaDataMap = doc.metaDataMap;
	}
	
	@Override
	public int hashCode() {
	    try {
	        OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
	        int hashCode = 314159 * getStartRevision().hashCode() + 271828 * getEndRevision().hashCode();
	        for (OntologyDocumentRevision revision = getStartRevision(); revision.compareTo(getEndRevision()) < 0; revision = revision.next()) {
	            hashCode = 42 * hashCode + getMetaData(revision).hashCode();
	            hashCode = hashCode - cropChanges(revision, revision.next()).getChanges(ontology).hashCode();
	        }
	        return hashCode;
	    }
	    catch (OWLOntologyCreationException e) {
	        throw new IllegalStateException("Could not create empty ontology");
	    }
	}

    @Override
    public boolean equals(Object o) {
    	if (!(o instanceof ChangeHistory)) {
    		return false;
    	}
    	ChangeHistory other = (ChangeHistory) o;
    	try {
    		OWLOntology ontology = OWLManager.createOWLOntologyManager().createOntology();
    		if (!(getStartRevision().equals(other.getStartRevision()) &&
                    getEndRevision().equals(other.getEndRevision()))) {
    		    return false;
    		}
    		for (OntologyDocumentRevision revision = getStartRevision(); revision.compareTo(getEndRevision()) < 0; revision = revision.next()) {
    		    if (!(getMetaData(revision).equals(other.getMetaData(revision)))) {
    		        return false;
    		    }
    		    if (!cropChanges(revision, revision.next()).getChanges(ontology)
    		            .equals(other.cropChanges(revision, revision.next()).getChanges(ontology))) {
    		        return false;
    		    }
    		}
    		return true;
    	}
    	catch (OWLOntologyCreationException e) {
    		throw new IllegalStateException("Could not create empty ontology");
    	}
    }

    @Override
    public String toString() {
    	return "{" + startRevision + " --> " + getEndRevision() + ": " + listOfRevisionChanges + "}";
    }

}
