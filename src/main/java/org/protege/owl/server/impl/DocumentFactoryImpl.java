package org.protege.owl.server.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.changes.ChangeDocumentImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class DocumentFactoryImpl implements DocumentFactory {

	@SuppressWarnings("deprecation")
	@Override
	public ChangeDocument createChangeDocument(List<OWLOntologyChange> changes,
											   Map<OntologyDocumentRevision, String> commitComments, 
											   OntologyDocumentRevision start) {
		return new ChangeDocumentImpl(start, changes);
	}

	@Override
	public VersionedOntologyDocument createSavedOntologyDocument(IRI localIRI,
			RemoteOntologyDocument serverDocument) throws IOException {
		throw new IllegalStateException("Not implemented yet");
	}

	@Override
	public VersionedOntologyDocument getSavedOntologyDocument(IRI localIRI)
			throws IOException {
		throw new IllegalStateException("Not implemented yet");

	}



}
