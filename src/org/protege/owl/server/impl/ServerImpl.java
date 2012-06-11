package org.protege.owl.server.impl;

import java.util.Collection;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.OntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.User;
import org.semanticweb.owlapi.model.IRI;

public class ServerImpl implements Server {

	@Override
	public ServerDocument getServerDocument(User u, IRI serverIRI) {
		throw new IllegalStateException("Not implemented yet");
	}

	@Override
	public Collection<ServerDocument> list(User u, ServerDirectory dir) {
		throw new IllegalStateException("Not implemented yet");

	}

	@Override
	public OntologyDocument create(User u, String commitComment, IRI serverIRI) {
		throw new IllegalStateException("Not implemented yet");

	}

	@Override
	public ChangeDocument getChanges(User u, OntologyDocument doc,
			OntologyDocumentRevision start, OntologyDocumentRevision end) {
		throw new IllegalStateException("Not implemented yet");

	}

	@Override
	public void applyChange(User u, OntologyDocument doc, String commitComment,
			OntologyDocumentRevision revision, ChangeDocument changes) {
		throw new IllegalStateException("Not implemented yet");
		
	}


}
