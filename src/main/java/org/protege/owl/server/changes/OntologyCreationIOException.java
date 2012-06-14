package org.protege.owl.server.changes;

import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class OntologyCreationIOException extends IOException {
	private static final long serialVersionUID = -4230547823218221533L;

	public OntologyCreationIOException(OWLOntologyCreationException e) {
		super(e);
	}

	@Override
	public OWLOntologyCreationException getCause() {
		return (OWLOntologyCreationException) super.getCause();
	}
}
