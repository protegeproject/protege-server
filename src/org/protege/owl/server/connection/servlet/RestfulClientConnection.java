package org.protege.owl.server.connection.servlet;


import static org.protege.owl.server.connection.servlet.OntologyListServlet.ONTOLOGY_MARKED_REVISION_PROPERTY;
import static org.protege.owl.server.connection.servlet.OntologyListServlet.ONTOLOGY_MAX_REVISION_PROPERTY;
import static org.protege.owl.server.connection.servlet.OntologyListServlet.ONTOLOGY_NAME_PROPERTY;
import static org.protege.owl.server.connection.servlet.OntologyListServlet.ONTOLOGY_SHORT_NAME_PROPERTY;
import static org.protege.owl.server.connection.servlet.OntologyListServlet.REMOTE_ONTOLOGY_CLASS;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.protege.owl.server.util.AbstractClientConnection;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class RestfulClientConnection extends AbstractClientConnection {
    private Logger logger = Logger.getLogger(RestfulClientConnection.class);
    private String httpPrefix;
    
    
    public RestfulClientConnection(OWLOntologyManager manager, String httpPrefix) {
        super(manager);
        this.httpPrefix = httpPrefix;
    }
    
    @Override
    public void commit(OWLOntology ontology) throws RemoteOntologyChangeException {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<RemoteOntologyRevisions> getRemoteOntologyList() {
        try {
            Set<RemoteOntologyRevisions> result = new HashSet<RemoteOntologyRevisions>();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntology(IRI.create(httpPrefix + OntologyListServlet.PATH));
            for (OWLIndividual revisions : REMOTE_ONTOLOGY_CLASS.getIndividuals(ontology)) {
                Set<Integer> markedRevisions = new HashSet<Integer>();
                for (OWLLiteral literal : revisions.getDataPropertyValues(ONTOLOGY_MARKED_REVISION_PROPERTY, ontology)) {
                    try {
                        markedRevisions.add(Integer.parseInt(literal.getLiteral()));
                    }
                    catch (NumberFormatException e) {
                        logger.info("Invalid data received from server - " + literal.getLiteral() + " is not an integer");
                    }
                }
                int maxRevision = -1;
                try {
                    maxRevision = Integer.parseInt(revisions.getDataPropertyValues(ONTOLOGY_MAX_REVISION_PROPERTY, ontology).iterator().next().getLiteral());
                }
                catch (NumberFormatException e) {
                    logger.info("Invalid data received from server");
                }
                IRI ontologyName = IRI.create(getStringValue(revisions, ONTOLOGY_NAME_PROPERTY, ontology));
                RemoteOntologyRevisions javaRevisions = new RemoteOntologyRevisions(ontologyName,
                                                                                    getStringValue(revisions, ONTOLOGY_SHORT_NAME_PROPERTY, ontology),
                                                                                    markedRevisions,
                                                                                    maxRevision);
                result.add(javaRevisions);
            }
            return result;
        }
        catch (OWLOntologyCreationException e) {
            throw new RPCRuntimeException(e);
        }
    }
    
    private String getStringValue(OWLIndividual i, OWLDataProperty p, OWLOntology ontology) {
        Set<OWLLiteral> values = i.getDataPropertyValues(p, ontology);
        if (values.size() > 1) {
            return values.iterator().next().getLiteral();
        }
        return null;
    }

    @Override
    public int getRevision(OWLOntology ontology) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public OWLOntology pull(IRI ontologyName, Integer revision) throws RemoteOntologyCreationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update(OWLOntology ontology, Integer revision) throws OWLOntologyChangeException {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<OWLOntology> getOntologies() {
        // TODO Auto-generated method stub
        return null;
    }

}
