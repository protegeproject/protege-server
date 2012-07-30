package org.protege.owl.server.changes;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.impl.DocumentFactoryImpl;
import org.protege.owl.server.util.LazyChangeDocument;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class ReallyLazySerializationTest extends AbstractSerializationTest {

	@Override
	protected DocumentFactory createDocumentFactory() {
		return new DocumentFactoryImpl() {
			@Override
			public ChangeDocument createChangeDocument(
					List<OWLOntologyChange> changes,
					Map<OntologyDocumentRevision, ChangeMetaData> commitComments,
					OntologyDocumentRevision start) {
				try {
					ChangeDocument changeDoc = super.createChangeDocument(changes, commitComments, start);
					File historyFile = File.createTempFile("Serialization", ChangeDocument.CHANGE_DOCUMENT_EXTENSION);
					ChangeDocumentUtilities.writeChanges(changeDoc, historyFile);
					return new LazyChangeDocument(historyFile);
				}
				catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
		};
	}
}
