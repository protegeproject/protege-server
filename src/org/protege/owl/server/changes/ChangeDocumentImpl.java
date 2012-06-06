package org.protege.owl.server.changes;

import java.util.List;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.OntologyDocument;
import org.protege.owl.server.api.ServerRevision;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class ChangeDocumentImpl implements ChangeDocument {

	@Override
	public OntologyDocument getOntologyDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerRevision getStartRevision() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerRevision getEndRevision() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChangeDocument cropChanges(ServerRevision start, ServerRevision end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

}
