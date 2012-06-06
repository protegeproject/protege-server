package org.protege.owl.server.impl;

import java.util.Collection;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.OntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.api.ServerRevision;
import org.protege.owl.server.api.User;
import org.semanticweb.owlapi.model.IRI;

public class ServerImpl implements Server {

	@Override
	public ServerPath getServerDocument(User u, IRI serverIRI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ServerPath> list(User u, ServerDirectory dir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChangeDocument getChanges(User u, OntologyDocument doc,
			ServerRevision start, ServerRevision end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OntologyDocument create(User u, String commitComment,
			OntologyDocument doc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void applyChange(User u, String commitComment, OntologyDocument doc,
			ServerRevision revision, ChangeDocument changes) {
		// TODO Auto-generated method stub

	}

}
