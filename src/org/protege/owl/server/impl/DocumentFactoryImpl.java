package org.protege.owl.server.impl;

import java.util.List;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.changes.ChangeDocumentImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class DocumentFactoryImpl implements DocumentFactory {

	@SuppressWarnings("deprecation")
	@Override
	public ChangeDocument createChangeDocument(List<OWLOntologyChange> changes, OntologyDocumentRevision start) {
		return new ChangeDocumentImpl(start, changes);
	}
	
	@Override
	public RemoteOntologyDocument createOntologyDocument(IRI localDoc, IRI serverAddress) {
		throw new IllegalStateException("Not implemented yet");
	}
	
	@Override
	public RemoteOntologyDocument loadOntologyDocument(IRI localDoc) {
		throw new IllegalStateException("Not implemented yet");
	}
	
	@Override
	public RemoteOntologyDocument saveOntologyDocument(RemoteOntologyDocument doc) {
		throw new IllegalStateException("Not implemented yet");
	}

}
