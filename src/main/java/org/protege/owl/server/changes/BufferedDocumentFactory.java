package org.protege.owl.server.changes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class BufferedDocumentFactory implements DocumentFactory, Serializable {
    private static final long serialVersionUID = -416185075845956547L;
    private int bufferSize = 1000;
	private DocumentFactory factory;
	
	public BufferedDocumentFactory(DocumentFactory factory) {
		this.factory = factory;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	@Override
	public ChangeDocument createEmptyChangeDocument(OntologyDocumentRevision revision) {
	    return new BufferedChangeDocument(this, factory.createEmptyChangeDocument(revision));
	}

	public ChangeDocument createChangeDocument(List<OWLOntologyChange> changes,
											   Map<OntologyDocumentRevision, ChangeMetaData> metaData,
			                                   OntologyDocumentRevision start) {
		return new BufferedChangeDocument(this, factory.createChangeDocument(changes, metaData, start));
	}

	public BufferedChangeDocument readChangeDocument(InputStream in,
	                                                  OntologyDocumentRevision start, OntologyDocumentRevision end)
	                                                 throws IOException {
	    try {
	        List<ChangeDocument> buffers = collectBuffers(in, start, end);
	        ChangeDocument doc = combineBuffers(buffers);
	        if (start == null) {
	            start = doc.getStartRevision();
	        }
	        if (end == null) {
	            end = doc.getEndRevision();
	        }
	        return new BufferedChangeDocument(this, doc.cropChanges(start, end));
	    }
	    catch (IOException ioe) {
	        throw ioe;
	    }
	    catch (Exception e) {
	        throw new IOException(e);
	    }
	}
	
	private List<ChangeDocument> collectBuffers(InputStream in,
	                                            OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException, ClassNotFoundException {
	    List<ChangeDocument> buffers = new ArrayList<ChangeDocument>();
	    ObjectInputStream ois;
	    if (in instanceof ObjectInputStream) {
	        ois = (ObjectInputStream) in;
	    }
	    else {
	        ois = new ObjectInputStream(in);
	    }
	    OntologyDocumentRevision startRevision = (OntologyDocumentRevision) ois.readObject();
	    OntologyDocumentRevision endRevision = (OntologyDocumentRevision) ois.readObject();
	    int myBufferSize = ois.readInt();
	    
	    byte[] array = new byte[0];
	    OntologyDocumentRevision windowEnd;
	    for (OntologyDocumentRevision windowStart = startRevision;
	            windowStart.compareTo(endRevision) < 0;
	            windowStart = windowEnd) {
	        windowEnd = windowStart.add(myBufferSize);
	        int length = (Integer) ois.readObject();
	        if (start != null && windowEnd.compareTo(start) <= 0) {
	            ois.skip(length);
	            continue;
	        }
	        if (end != null && windowStart.compareTo(end) >= 0) {
	            ois.skip(length);
	            continue;
	        }
	        if (array.length < length) {
	            array = new byte[length];
	        }
	        for (int bytesRead = 0; bytesRead < length; ) {
	            bytesRead += ois.read(array, bytesRead, length - bytesRead);
	        }
	        ByteArrayInputStream bais = new ByteArrayInputStream(array, 0, length);
	        ChangeDocument newChangeDoc = factory.readChangeDocument(bais, windowStart, windowEnd);
	        buffers.add(newChangeDoc);
	    }
	    return buffers;
	}
	
	private ChangeDocument combineBuffers(List<ChangeDocument> buffers) throws OWLOntologyCreationException {
	    OntologyDocumentRevision startRevision = buffers.get(0).getStartRevision();
	    List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
	    Map<OntologyDocumentRevision, ChangeMetaData> metaData = new TreeMap<OntologyDocumentRevision, ChangeMetaData>();
	    OWLOntology fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
	    for (ChangeDocument buffer : buffers) {
	        changes.addAll(buffer.getChanges(fakeOntology));
	        metaData.putAll(buffer.getMetaData());
	    }
	    return factory.createChangeDocument(changes, metaData, startRevision);
	}

	public boolean hasServerMetadata(OWLOntology ontology) {
		return factory.hasServerMetadata(ontology);
	}

	public VersionedOWLOntology createVersionedOntology(OWLOntology ontology)
			throws IOException {
		return new BufferedVersionedOntology(this, factory.createVersionedOntology(ontology));
	}

	public VersionedOWLOntology createVersionedOntology(OWLOntology ontology,
			RemoteOntologyDocument serverDocument,
			OntologyDocumentRevision revision) {
		return new BufferedVersionedOntology(this, 
											 factory.createVersionedOntology(ontology,
				                                                             serverDocument,
	          	 									                         revision));
	}
	
	
}
