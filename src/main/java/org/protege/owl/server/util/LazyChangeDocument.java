package org.protege.owl.server.util;

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
	private transient File historyFile;
	private ChangeDocument delegate;
	
	public LazyChangeDocument(ChangeDocument delegate) {
		this.delegate = delegate;
	}
	
	public LazyChangeDocument(File historyFile) {
		this.historyFile = historyFile;
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
		}
	}

	@Override
	public OntologyDocumentRevision getStartRevision() {
		ensureDelegateLoaded();
		return delegate.getStartRevision();
	}

	@Override
	public OntologyDocumentRevision getEndRevision() {
		ensureDelegateLoaded();
		return delegate.getEndRevision();
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
		out.writeObject(new Boolean(delegate != null));
		if (delegate != null) {
			out.writeObject(delegate);
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
		boolean hasDelegate = (Boolean) in.readObject();
		if (hasDelegate) {
			delegate = (ChangeDocument) in.readObject();
		}
		else {
			File tmpFile = File.createTempFile("LazyChangeDoc", ChangeDocument.CHANGE_DOCUMENT_EXTENSION);
			tmpFile.deleteOnExit();
			OutputStream os = new FileOutputStream(tmpFile);
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
	}
	
}
