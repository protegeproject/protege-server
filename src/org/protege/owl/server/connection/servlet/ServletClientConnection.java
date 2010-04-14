package org.protege.owl.server.connection.servlet;

import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MARKED_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MAX_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_SHORT_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_CLASS;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.server.api.ClientConnection;
import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.exception.InvalidRemoteDataException;
import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ServletClientConnection implements ClientConnection {
	private OWLOntologyManager manager;
	private String httpPrefix;
	private Map<OWLOntology, OntologyInfo> ontologyInfoMap = new HashMap<OWLOntology, OntologyInfo>();
	private Set<RemoteOntologyRevisions> revisions;
	
	public ServletClientConnection(OWLOntologyManager manager, String host) {
		this.manager = manager;
		httpPrefix = "http://" + host;
	}
	
	@Override
	public OWLOntologyManager getOntologyManager() {
		return manager;
	}

	@Override
	public Set<OWLOntology> getOntologies() {
		return ontologyInfoMap.keySet();
	}
	
	@Override
	public Set<RemoteOntologyRevisions> getRemoteOntologyList() throws RemoteOntologyCreationException {
		if (revisions == null) {
			revisions = updateRemoteOntologyList();
		}
		return revisions;
	}

	@Override
	public Set<RemoteOntologyRevisions> updateRemoteOntologyList() throws RemoteOntologyCreationException {
		Set<RemoteOntologyRevisions> result = new HashSet<RemoteOntologyRevisions>();
		try {
			OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
			OWLOntology response = otherManager.loadOntologyFromOntologyDocument(IRI.create(httpPrefix + OntologyListServlet.PATH));
			for (OWLIndividual i : REMOTE_ONTOLOGY_CLASS.getIndividuals(response)) {
				OWLLiteral ontologyName = i.getDataPropertyValues(ONTOLOGY_NAME_PROPERTY, response).iterator().next();
				OWLLiteral shortName = i.getDataPropertyValues(ONTOLOGY_SHORT_NAME_PROPERTY, response).iterator().next();
				Set<Integer> markedRevisions = new HashSet<Integer>();
				for (OWLLiteral value : i.getDataPropertyValues(ONTOLOGY_MARKED_REVISION_PROPERTY, response)) {
					markedRevisions.add(Integer.parseInt(value.getLiteral()));
				}
				OWLLiteral maxRevision = i.getDataPropertyValues(ONTOLOGY_MAX_REVISION_PROPERTY, response).iterator().next();
				result.add(new RemoteOntologyRevisions(IRI.create(ontologyName.getLiteral()), 
													   shortName.getLiteral(), 
													   markedRevisions, 
													   Integer.parseInt(maxRevision.getLiteral())));
			}
		}
		catch (OWLOntologyCreationException e) {
			new InvalidRemoteDataException("Could not parse remote owl file", e);
		}
		catch (NumberFormatException e) {
			new InvalidRemoteDataException("Could not parse remote owl file", e);
		}
		return result;
	}

	public OWLOntology pull(IRI ontologyName, Integer revisionToGet)
			throws RemoteOntologyCreationException {
		RemoteOntologyRevisions revisions = null;
		for (RemoteOntologyRevisions tryMe : getRemoteOntologyList()) {
			if (tryMe.getOntologyName().equals(ontologyName)) {
				revisions = tryMe;
			}
		}
		if (revisions == null) {
			return null;
		}
		Integer closestRevision = null;
		for (int markedRevision : revisions.getMarkedRevisions()) {
			if (revisionToGet != null && markedRevision > revisionToGet) {
				continue;
			}
			if (closestRevision != null && markedRevision <= closestRevision) {
				continue;
			}
			closestRevision = markedRevision;
		}
		if (closestRevision == null) {
			return null;
		}
		IRI physicalLocation = IRI.create(httpPrefix + MarkedOntologyServlet.PATH + "/");

		return null;
	}

	public int getRevision(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(OWLOntology ontology, Integer revision)
			throws OWLOntologyChangeException {
		// TODO Auto-generated method stub
	
	}

	public List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	public void commit(OWLOntology ontology)
			throws RemoteOntologyChangeException {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub
	
	}
	
	private static class OntologyInfo {
		private List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		private int revision;
		
		public OntologyInfo(int revision) {
			super();
			this.revision = revision;
		}
		public int getRevision() {
			return revision;
		}
		public void setRevision(int revision) {
			this.revision = revision;
		}
		public List<OWLOntologyChange> getChanges() {
			return changes;
		}
		
		public void addChanges(List<OWLOntologyChange> newChanges) {
			changes.addAll(newChanges);
		}
		
		public List<OWLOntologyChange> clearChanges() {
			List<OWLOntologyChange> oldChanges = changes;
			changes = new ArrayList<OWLOntologyChange>();
			return oldChanges;
		}
		
	}

}
