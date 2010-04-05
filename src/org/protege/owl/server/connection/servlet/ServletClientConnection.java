package org.protege.owl.server.connection.servlet;

import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MARKED_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MAX_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_SHORT_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_CLASS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
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
	private static Logger logger = Logger.getLogger(ServletClientConnection.class);
	private OWLOntologyManager manager;
	private String httpPrefix;
	
	public ServletClientConnection(OWLOntologyManager manager, String host) {
		this.manager = manager;
		httpPrefix = "http://" + host;
	}
	
	public OWLOntologyManager getOntologyManager() {
		return manager;
	}

	public Set<RemoteOntologyRevisions> getRemoteOntologyList() throws RemoteOntologyCreationException {
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

	public void commit(OWLOntology ontology)
			throws RemoteOntologyChangeException {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public int getRevision(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology) {
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology pull(IRI ontologyName, Integer revision)
			throws RemoteOntologyCreationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void update(OWLOntology ontology, Integer revision)
			throws OWLOntologyChangeException {
		// TODO Auto-generated method stub

	}

	public Set<OWLOntology> getOntologies() {
		// TODO Auto-generated method stub
		return null;
	}

}
