package org.protege.owl.server.changes;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class OntologyStorageIOException extends IOException {
	private static final long serialVersionUID = 966907367321237676L;

	public OntologyStorageIOException(OWLOntologyStorageException e) {
		super(e);
	}
	
	@Override
	public OWLOntologyStorageException getCause() {
		return (OWLOntologyStorageException) super.getCause();
	}
}

