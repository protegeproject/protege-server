package org.protege.owl.server.changes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

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

	@Override
	public OntologyDocumentRevision getStartRevision() {
		return delegate.getStartRevision();
	}

	@Override
	public OntologyDocumentRevision getEndRevision() {
		return delegate.getEndRevision();
	}

	@Override
	public SortedMap<OntologyDocumentRevision, ChangeMetaData> getMetaData() {
		return delegate.getMetaData();
	}

	@Override
	public ChangeDocument cropChanges(OntologyDocumentRevision start,
			OntologyDocumentRevision end) {
		return delegate.cropChanges(start, end);
	}

    @Override
	public ChangeDocument appendChanges(ChangeDocument additionalChanges) {
		return delegate.appendChanges(additionalChanges);
	}

    @Override
	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
		return delegate.getChanges(ontology);
	}
    
    @Override
    public List<OWLOntologyChange> getChanges(OWLOntology ontology, Set<OntologyDocumentRevision> toIgnore) {
        return delegate.getChanges(ontology, toIgnore);
    }
	
	@Override
	public int size() {
	    return delegate.size();
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
	        oos.writeObject((Integer) baos.toByteArray().length);
	        oos.write(baos.toByteArray());
	    }
	    oos.flush();
	}
	
	@Override
	public boolean equals(Object obj) {
	    return delegate.equals(obj);
	}
	
	@Override
	public int hashCode() {
	    return delegate.hashCode();
	}
	
	@Override
	public String toString() {
	    return delegate.toString();
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
