package org.protege.owl.server.changes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class BufferedChangeDocument implements ChangeDocument, Serializable {
    private static final long serialVersionUID = 1345665898488726397L;
    private transient ChangeDocument delegate;
	private BufferedDocumentFactory factory;
	
	public BufferedChangeDocument(BufferedDocumentFactory factory, ChangeDocument delegate) {
		this.delegate = delegate;
		this.factory = factory;
	}
	
	@Override
	public BufferedDocumentFactory getDocumentFactory() {
		return factory;
	}

	public OntologyDocumentRevision getStartRevision() {
		return delegate.getStartRevision();
	}

	public OntologyDocumentRevision getEndRevision() {
		return delegate.getEndRevision();
	}

	public Map<OntologyDocumentRevision, ChangeMetaData> getMetaData() {
		return delegate.getMetaData();
	}

	public ChangeDocument cropChanges(OntologyDocumentRevision start,
			OntologyDocumentRevision end) {
		return delegate.cropChanges(start, end);
	}

	public ChangeDocument appendChanges(ChangeDocument additionalChanges) {
		return delegate.appendChanges(additionalChanges);
	}

	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
		return delegate.getChanges(ontology);
	}

	public void writeChangeDocument(OutputStream out) throws IOException {
	    ObjectOutputStream oos;
	    if (out instanceof ObjectOutputStream) {
	        oos = (ObjectOutputStream) out; 
	    }
	    else {
	        oos = new ObjectOutputStream(out);
	    }
	    oos.writeObject(delegate.getStartRevision());
	    oos.writeObject(delegate.getEndRevision());
	    oos.writeInt(factory.getBufferSize());
	    OntologyDocumentRevision windowEnd;
	    for (OntologyDocumentRevision windowStart = delegate.getStartRevision();
	            windowStart.compareTo(delegate.getEndRevision()) < 0;
	            windowStart = windowEnd) {
	        windowEnd = windowStart.add(factory.getBufferSize());
	        if (windowEnd.compareTo(getEndRevision()) > 0) {
	            windowEnd = getEndRevision();
	        }
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        cropChanges(windowStart, windowEnd).writeChangeDocument(baos);
	        oos.writeObject(baos.toByteArray());
	    }
	}
	
	@Override
	public boolean equals(Object obj) {
	    return delegate.equals(obj);
	}
	
	@Override
	public int hashCode() {
	    return delegate.hashCode();
	}
	
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(getDocumentFactory());
        writeChangeDocument(out);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        factory = (BufferedDocumentFactory) in.readObject();
        BufferedChangeDocument doc = (BufferedChangeDocument) factory.readChangeDocument(in, null, null);
        delegate = doc.delegate;
    }
    
    @SuppressWarnings("unused")
    private void readObjectNoData() throws ObjectStreamException {
        throw new IllegalStateException("huh?");
    }

}
