package org.protege.owl.server.connection.servlet;

import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MARKED_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MAX_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_SHORT_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_CLASS;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.connection.servlet.serialize.Serializer;
import org.protege.owl.server.connection.servlet.serialize.SerializerFactory;
import org.protege.owl.server.exception.OntologyConflictException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owl.server.util.AbstractClientConnection;
import org.protege.owl.server.util.AxiomToChangeConverter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ServletClientConnection extends AbstractClientConnection {
	private String httpPrefix;
	private Serializer serializer;
	
	public ServletClientConnection(OWLOntologyManager manager, String host) {
		super(manager);
		httpPrefix = "http://" + host;
		serializer = new SerializerFactory().createSerializer();
	}
	


    /* *****************************************************************************
     * Abstract methods from AbstractClientConnection.
     */
    
	@Override
    protected Set<RemoteOntologyRevisions> updateRemoteOntologyList() throws RemoteQueryException {
        Set<RemoteOntologyRevisions> result = new HashSet<RemoteOntologyRevisions>();
        try {
            OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
            OWLOntology response = serializer.deserialize(otherManager, new URL(httpPrefix + OntologyListServlet.PATH));
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
        catch (IOException e) {
            throw new RemoteQueryException("Unexpected IO Exception", e);
        }
        catch (NumberFormatException e) {
            new RemoteQueryException("Could not parse remote owl file", e);
        }
        catch (OntologyConflictException ioe) {
            throw new IllegalStateException("This shouldn't happen", ioe);
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
        if (start == end) {
            return Collections.emptyList();
        }
        try {
            OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
            OWLOntology changeOntology = serializer.deserialize(otherManager, new URL(httpPrefix + OntologyDeltaServlet.PATH + "/" + shortName + "/" + start + "/" + end));
            AxiomToChangeConverter converter = new AxiomToChangeConverter(ontology);
            List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
            for (OWLAxiom changeAxiom : changeOntology.getAxioms()) {
                changeAxiom.accept(converter);
                changes.add(converter.getChange());
            }
            return changes;
        }
        catch (IOException e) {
            throw new RemoteQueryException("Unexpected IO Exception", e);
        }
        catch (OntologyConflictException e) {
            throw new IllegalStateException("This shouldn't happen", e);
        }
	}
	
    /* *****************************************************************************
     * Interface implementations.
     */

	@Override
	public void commit(Set<OWLOntology> ontologies) throws OntologyConflictException {
	    throw new UnsupportedOperationException("Not implemented yet");
	}
}
