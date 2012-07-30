package org.protege.owl.server.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class LazyChangeDocument implements ChangeDocument {
	private static final long serialVersionUID = -2372361709521265014L;
	private OntologyDocumentRevision startRevision;
	private OntologyDocumentRevision endRevision;
	private transient File historyFile;
	private ChangeDocument delegate;
	
	public LazyChangeDocument(ChangeDocument delegate) {
		this.delegate = delegate;
		this.startRevision = delegate.getStartRevision();
		this.endRevision = delegate.getEndRevision();
	}
	
	public ChangeDocument getDelegate() {
		return delegate;
	}
	
	private void ensureDelegateLoaded() {
		if (delegate == null) {
			ObjectInputStream oin = null;
			try {
				try {
					InputStream in = new FileInputStream(historyFile);
					oin = new ObjectInputStream(in);
					delegate = (ChangeDocument) oin.readObject();
				}
				finally {
					if (oin != null) {
						oin.close();
					}
				}
			}
			catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			catch (ClassNotFoundException cnfe) {
				throw new RuntimeException(cnfe);
			}
			historyFile.delete();
		}
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
		ensureDelegateLoaded();
		return delegate.getMetaData();
	}

	@Override
	public ChangeDocument cropChanges(OntologyDocumentRevision start,
			OntologyDocumentRevision end) {
		ensureDelegateLoaded();
		return delegate.cropChanges(start, end);
	}

	@Override
	public ChangeDocument appendChanges(ChangeDocument additionalChanges) {
		ensureDelegateLoaded();
		return delegate.appendChanges(additionalChanges);
	}

	@Override
	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
		ensureDelegateLoaded();
		return delegate.getChanges(ontology);
	}
	
	@Override
	public boolean equals(Object obj) {
		ensureDelegateLoaded();
		return delegate.equals(obj);
	}
	
	@Override
	public int hashCode() {
		ensureDelegateLoaded();
		return delegate.hashCode();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(startRevision);
		out.writeObject(endRevision);
		if (delegate != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream delegateStream = new ObjectOutputStream(baos);
			try {
				delegateStream.writeObject(delegate);
			}
			finally {
				delegateStream.flush();
				delegateStream.close();
			}
			try {
				out.write(baos.toByteArray());
			}
			finally {
				out.flush();
			}
		}
		else {
			InputStream fin = new FileInputStream(historyFile);
			try {
				int b;
				while ((b = fin.read()) >= 0) {
					out.write(b);
				}
			}
			finally {
				fin.close();
				out.flush();
			}
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		startRevision = (OntologyDocumentRevision) in.readObject();
		endRevision = (OntologyDocumentRevision) in.readObject();
		File tmpFile = File.createTempFile("LazyChangeDoc", ChangeDocument.CHANGE_DOCUMENT_EXTENSION);
		tmpFile.deleteOnExit();
		OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile));
		try {
			int b;
			while ((b = in.read()) >= 0) {
				os.write(b);
			}
		}
		finally {
			os.flush();
			os.close();
		}
		historyFile = tmpFile;
	}
	
	@Override
	protected void finalize() throws Throwable {
		historyFile.delete();
	}
	
}
