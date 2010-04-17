package org.protege.owl.server.connection.servlet;

import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MARKED_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MAX_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_SHORT_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_CLASS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.exception.InvalidRemoteDataException;
import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owl.server.util.AbstractClientConnection;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ServletClientConnection extends AbstractClientConnection {
	private String httpPrefix;
	private Set<RemoteOntologyRevisions> revisions;
	
	public ServletClientConnection(OWLOntologyManager manager, String host) {
		super(manager);
		httpPrefix = "http://" + host;
	}
	


    /* *****************************************************************************
     * Abstract methods from AbstractClientConnection.
     */
    
	@Override
    protected Set<RemoteOntologyRevisions> updateRemoteOntologyList() throws RemoteQueryException {
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
            new RemoteQueryException("Could not parse remote owl file", e);
        }
        catch (NumberFormatException e) {
            new InvalidRemoteDataException("Could not parse remote owl file", e);
        }
        return result;
    }
    
    @Override
    protected OWLOntology pullMarked(IRI ontologyName, String shortName, int revisionToGet) throws OWLOntologyCreationException, RemoteQueryException {
        IRI physicalLocation = IRI.create(httpPrefix + MarkedOntologyServlet.PATH + "/" + shortName + "/" + revisionToGet);
        return getOntologyManager().loadOntologyFromOntologyDocument(physicalLocation);
    }
    
	@Override
	protected List<OWLOntologyChange> getChangesFromServer(OWLOntology ontology, String shortName, int start, int end) throws RemoteQueryException {
	    // TODO Auto-generated method stub
	    if (start == end) {
	        return Collections.emptyList();
	    }
	    OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
	    return Collections.emptyList();
	}
	
    /* *****************************************************************************
     * Interface implementations.
     */

	@Override
	public void commit(Set<OWLOntology> ontologies) throws RemoteOntologyChangeException {
	    
	}
}
