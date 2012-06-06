package org.protege.owl.server.connect;

import java.util.Collection;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeDocumentFactory;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.OntologyDocument;
import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.api.ServerRevision;
import org.semanticweb.owlapi.model.IRI;

public class RawClientTransport implements Client {

	@Override
	public ChangeDocumentFactory getChangeDocumentFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OntologyDocument getOntologyDocument(IRI ontologyIRI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerRevision getServerRevision(IRI ontologyIRI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ServerPath> list(ServerPath path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void create(String commitComment, OntologyDocument doc) {
		// TODO Auto-generated method stub

	}

	@Override
	public ChangeDocument getChanges(OntologyDocument document,
			ServerRevision start, ServerRevision end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commit(OntologyDocument document, ServerRevision revision,
			ChangeDocument changes) {
		// TODO Auto-generated method stub

	}

}
