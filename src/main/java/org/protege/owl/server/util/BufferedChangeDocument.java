package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class BufferedChangeDocument implements ChangeDocument {
	private static final long serialVersionUID = 6987984736921178953L;

	public static final int DEFAULT_BUFFER_SIZE = 1000;
	
	private int bufferSize;
	private OntologyDocumentRevision startRevision;
	private OntologyDocumentRevision endRevision;
	private List<LazyChangeDocument> buffers = new ArrayList<LazyChangeDocument>();
	private transient List<OWLOntologyChange> changes;
	private transient Map<OntologyDocumentRevision, ChangeMetaData> metaData;
	
	public BufferedChangeDocument(ChangeDocument changes) {
		this(changes, DEFAULT_BUFFER_SIZE);
	}
	
	private BufferedChangeDocument() {
		;
	}
	
	public BufferedChangeDocument(ChangeDocument changeDoc, int bufferSize) {
		this.bufferSize = bufferSize;
		this.startRevision = changeDoc.getStartRevision();
		this.endRevision  = changeDoc.getEndRevision();
		
	}
		
	public int getBufferSize() {
		return bufferSize;
	}

	@Override
	public OntologyDocumentRevision getStartRevision() {
		return startRevision;
	}

	@Override
	public OntologyDocumentRevision getEndRevision() {
		return endRevision;
	}

	@Override
	public Map<OntologyDocumentRevision, ChangeMetaData> getMetaData() {
		if (this.metaData == null) {
			Map<OntologyDocumentRevision, ChangeMetaData> metaData = new TreeMap<OntologyDocumentRevision, ChangeMetaData>();
			for (ChangeDocument subDocument : buffers) {
				metaData.putAll(subDocument.getMetaData());
			}
			this.metaData = metaData;
		}
		return this.metaData;
	}

	@Override
	public BufferedChangeDocument cropChanges(OntologyDocumentRevision start,
			                          OntologyDocumentRevision end) {
		List<LazyChangeDocument> newBuffers = new ArrayList<LazyChangeDocument>();
		for (LazyChangeDocument subDocument : buffers) {
			if (subDocument.getStartRevision().compareTo(end) <= 0 && subDocument.getEndRevision().compareTo(start) >=0) {
				newBuffers.add(subDocument);
			}
		}
		BufferedChangeDocument cropped = new BufferedChangeDocument();
		cropped.bufferSize = bufferSize;
		cropped.startRevision = start;
		cropped.endRevision  = end;
		cropped.buffers = newBuffers;
		return cropped;
	}

	@Override
	public BufferedChangeDocument appendChanges(ChangeDocument additionalChanges) {
		if (additionalChanges.getEndRevision().compareTo(getEndRevision()) <= 0) {
			return this;
		}
		BufferedChangeDocument newChangeDocument = new BufferedChangeDocument();
		newChangeDocument.bufferSize = bufferSize;
		newChangeDocument.startRevision = startRevision;
		newChangeDocument.endRevision = additionalChanges.getEndRevision();
		newChangeDocument.buffers = new ArrayList<LazyChangeDocument>(buffers);
		LazyChangeDocument lastBuffer = buffers.get(buffers.size() - 1);
		OntologyDocumentRevision lastBufferEndpoint = new OntologyDocumentRevision(lastBuffer.getStartRevision().getRevision() + bufferSize);
		if (lastBuffer.getEndRevision().getRevision() < lastBuffer.getStartRevision().getRevision() + bufferSize) {
			ChangeDocument newLastBuffer = lastBuffer.appendChanges(additionalChanges.cropChanges(endRevision, lastBufferEndpoint));
			newChangeDocument.buffers.remove(buffers.size() - 1);
			newChangeDocument.buffers.add(new LazyChangeDocument(newLastBuffer));
		}
		while (lastBufferEndpoint.compareTo(additionalChanges.getEndRevision()) <= 0) {
			OntologyDocumentRevision newLastBufferEndpoint = new OntologyDocumentRevision(lastBufferEndpoint.getRevision() + bufferSize);
			newChangeDocument.buffers.add(new LazyChangeDocument(additionalChanges.cropChanges(lastBufferEndpoint, newLastBufferEndpoint)));
			lastBufferEndpoint = newLastBufferEndpoint;
		}
		return newChangeDocument;
	}

	@Override
	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
		if (this.changes == null) {
			List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
			for (ChangeDocument subDocument : buffers) {
				changes.addAll(subDocument.getChanges(ontology));
			}
			this.changes = changes;
		}
		return this.changes;
	}

}
