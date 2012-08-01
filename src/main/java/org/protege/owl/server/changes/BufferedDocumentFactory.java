package org.protege.owl.server.changes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

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

	public ChangeDocument createChangeDocument(List<OWLOntologyChange> changes,
											   Map<OntologyDocumentRevision, ChangeMetaData> metaData,
			                                   OntologyDocumentRevision start) {
		return new BufferedChangeDocument(this, factory.createChangeDocument(changes, metaData, start));
	}

	public BufferedChangeDocument readChangeDocument(InputStream in,
	                                                  OntologyDocumentRevision start, OntologyDocumentRevision end)
	                                                 throws IOException {
        ChangeDocument doc = null;
	    try {
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
	        OntologyDocumentRevision windowEnd;
	        for (OntologyDocumentRevision windowStart = startRevision;
	                windowStart.compareTo(endRevision) < 0;
	                windowStart = windowEnd) {
	            windowEnd = windowStart.add(myBufferSize);
	            if (start != null && windowEnd.compareTo(start) <= 0) {
	                ois.readObject();
	                continue;
	            }
	            if (end != null && windowStart.compareTo(end) >= 0) {
	                ois.readObject();
	                continue;
	            }
	            byte[] array = (byte[]) ois.readObject();
	            ByteArrayInputStream bais = new ByteArrayInputStream(array);
	            ChangeDocument newChangeDoc = factory.readChangeDocument(bais, windowStart, windowEnd);
	            if (doc == null) {
	                doc = newChangeDoc;
	            }
	            else {
	                doc = doc.appendChanges(newChangeDoc);
	            }
	        }
	    }
	    catch (ClassNotFoundException cnfe) {
	        throw new IOException(cnfe);
	    }
	    if (start == null) {
	        start = doc.getStartRevision();
	    }
	    if (end == null) {
	        end = doc.getEndRevision();
	    }
	    return new BufferedChangeDocument(this, doc.cropChanges(start, end));
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
