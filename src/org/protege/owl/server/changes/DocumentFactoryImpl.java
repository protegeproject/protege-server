package org.protege.owl.server.changes;

import java.util.List;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocument;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class DocumentFactoryImpl implements DocumentFactory {

	@SuppressWarnings("deprecation")
	@Override
	public ChangeDocument createChangeDocument(List<OWLOntologyChange> changes, OntologyDocumentRevision start) {
		return new ChangeDocumentImpl(start, changes);
	}
	
	@Override
	public OntologyDocument createOntologyDocument(IRI location) {
		throw new IllegalStateException("Not implemented yet");
	}
	
	@Override
	public OntologyDocument loadOntologyDocument(IRI location) {
		throw new IllegalStateException("Not implemented yet");
	}

}
