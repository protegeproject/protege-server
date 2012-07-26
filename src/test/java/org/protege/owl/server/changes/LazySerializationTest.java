package org.protege.owl.server.changes;

import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.impl.DocumentFactoryImpl;
import org.protege.owl.server.util.LazyChangeDocument;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class LazySerializationTest extends AbstractSerializationTest {

	@Override
	protected DocumentFactory createDocumentFactory() {
		return new DocumentFactoryImpl() {
			@Override
			public ChangeDocument createChangeDocument(
					List<OWLOntologyChange> changes,
					Map<OntologyDocumentRevision, String> commitComments,
					OntologyDocumentRevision start) {
				// TODO Auto-generated method stub
				return new LazyChangeDocument(super.createChangeDocument(changes, commitComments, start));
			}
		};
	}

}
