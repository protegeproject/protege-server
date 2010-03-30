package org.protege.owl.server.connection.servlet;

import java.util.List;
import java.util.Set;

import org.protege.owl.server.api.ClientConnection;
import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ServletClientConnection implements ClientConnection {
	private OWLOntologyManager manager;
	private String httpPrefix;
	
	public ServletClientConnection(OWLOntologyManager manager, String host) {
		this.manager = manager;
		httpPrefix = "http://" + host;
	}
	
	@Override
	public void commit(OWLOntology ontology)
			throws RemoteOntologyChangeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public OWLOntologyManager getOntologyManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<RemoteOntologyRevisions> getRemoteOntologyList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRevision(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLOntology pull(IRI ontologyName, Integer revision)
			throws RemoteOntologyCreationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(OWLOntology ontology, Integer revision)
			throws OWLOntologyChangeException {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<OWLOntology> getOntologies() {
		// TODO Auto-generated method stub
		return null;
	}

}
