package org.protege.owl.server.connection.servlet;

import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MARKED_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MAX_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_SHORT_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_CLASS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.protege.owl.server.api.ServerOntologyInfo;
import org.protege.owl.server.exception.OntologyConflictException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owl.server.util.AbstractClientConnection;
import org.protege.owl.server.util.ChangeAndRevisionSummary;
import org.protege.owl.server.util.ChangeToAxiomConverter;
import org.protege.owl.server.util.ChangeUtilities;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class ServletClientConnection extends AbstractClientConnection {
    public static final Logger LOGGER = Logger.getLogger(ServletClientConnection.class);
    
	private String httpPrefix;
	private Serializer serializer;
	
	public ServletClientConnection(ProtegeOWLOntologyManager manager, String host) {
		super(manager);
		httpPrefix = "http://" + host;
		serializer = new SerializerFactory().createSerializer();
	}
	


    /* *****************************************************************************
     * Abstract methods from AbstractClientConnection.
     */
    
	@Override
    protected Set<ServerOntologyInfo> getRemoteOntologyList() throws RemoteQueryException {
        Set<ServerOntologyInfo> result = new HashSet<ServerOntologyInfo>();
        try {
            OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
            OWLOntology response = serializer.deserialize(otherManager, new URL(httpPrefix + Paths.ONTOLOGY_LIST_PATH));
       
            for (OWLIndividual i : REMOTE_ONTOLOGY_CLASS.getIndividuals(response)) {
                OWLLiteral ontologyName = i.getDataPropertyValues(ONTOLOGY_NAME_PROPERTY, response).iterator().next();
                OWLLiteral shortName = i.getDataPropertyValues(ONTOLOGY_SHORT_NAME_PROPERTY, response).iterator().next();
                Set<Integer> markedRevisions = new HashSet<Integer>();
                for (OWLLiteral value : i.getDataPropertyValues(ONTOLOGY_MARKED_REVISION_PROPERTY, response)) {
                    Integer revision = Integer.parseInt(value.getLiteral());
                    markedRevisions.add(revision);
                }
                OWLLiteral maxRevision = i.getDataPropertyValues(ONTOLOGY_MAX_REVISION_PROPERTY, response).iterator().next();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Latest revision for " + ontologyName + "(" + shortName + ") = " + Integer.parseInt(maxRevision.getLiteral()));
                }
                result.add(new ServerOntologyInfo(IRI.create(ontologyName.getLiteral()), 
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
        IRI physicalLocation = IRI.create(httpPrefix + Paths.MARKED_ONTOLOGY_PATH + "/" + shortName + "/" + revisionToGet);
        return getOntologyManager().loadOntologyFromOntologyDocument(physicalLocation);
    }
    
    @Override
    protected ChangeAndRevisionSummary getChangesFromServer(OWLOntology ontology, String shortName, int start, int end) throws RemoteQueryException {
        if (start == end) {
            ChangeAndRevisionSummary summary = new ChangeAndRevisionSummary();
            summary.setChanges(new ArrayList<OWLOntologyChange>());
            summary.setRevisions(new HashMap<IRI, Integer>());
            return summary;
        }
        try {
            OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
            OWLOntology changeOntology = serializer.deserialize(otherManager, new URL(httpPrefix + Paths.ONTOLOGY_DELTA_PATH + "/" + shortName + "/" + start + "/" + end));
            if (LOGGER.isDebugEnabled()) {
                ChangeUtilities.logOntology("Retrieving changes for " + shortName + " from " + start + " to " + end, changeOntology, LOGGER, Level.DEBUG);
            }
            return ChangeAndRevisionSummary.getChanges(getOntologies(), changeOntology);
        }
        catch (IOException e) {
            throw new RemoteQueryException("Unexpected IO Exception", e);
        }
        catch (OntologyConflictException e) {
            throw new IllegalStateException("This shouldn't happen", e);
        }
	}

    // TODO break this into smaller pieces!
	@Override
	protected void internalCommit(Set<OWLOntology> allOntologies, List<OWLOntologyChange> allChanges) throws OntologyConflictException, RemoteQueryException {
	    if (LOGGER.isDebugEnabled()) {
	        LOGGER.debug("Commit started");
	    }
	    OWLOntology metaOntology;
	    try {
	        synchronized (this) {
	            ChangeUtilities.logChanges("Found uncommitted changes to commit", allChanges, LOGGER, Level.DEBUG);
	            metaOntology = getRequestCommitOntology(allOntologies, allChanges);
	        }
	        URL servlet = new URL(httpPrefix + Paths.ONTOLOGY_COMMIT_PATH);
	        URLConnection connection = servlet.openConnection();
	        connection.setDoOutput(true);
	        connection.connect();
	        ChangeUtilities.logOntology("Sending commit ontology:", metaOntology, LOGGER, Level.DEBUG);
	        serializer.serialize(metaOntology, connection.getOutputStream());
	        int responseCode = ((HttpURLConnection) connection).getResponseCode();
	        if (responseCode != HttpURLConnection.HTTP_CONFLICT) {
	            if (LOGGER.isDebugEnabled()) {
	                LOGGER.debug("Response code " + responseCode + " deemed successful - clearing uncommitted changes");
	            }
	            clearUncommittedChanges(allChanges);
	            OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
	            OWLOntology changeOntology = serializer.deserialize(otherManager, new StreamDocumentSource(connection.getInputStream()));
	            handleRemoteChanges(changeOntology);
	        }
	        else {
	            if (LOGGER.isDebugEnabled()) {
	                LOGGER.debug("Conflict detected on server");
	            }
	            OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
                OWLOntology rejectedOntology = serializer.deserialize(otherManager, new StreamDocumentSource(((HttpURLConnection) connection).getErrorStream()));
	            ChangeAndRevisionSummary rejectedSummary = ChangeAndRevisionSummary.getChanges(getOntologies(), rejectedOntology);
	            throw new OntologyConflictException(rejectedSummary.getChanges());
	        }
	    }
	    catch (OntologyConflictException e) {
	        throw e;
	    }
	    catch (Exception e) {
	        throw new RemoteQueryException(e);
	    }
	}
	
    
    /* *****************************************************************************
     * Interface implementations.
     */


	private OWLOntology getRequestCommitOntology(Set<OWLOntology> ontologies, Collection<OWLOntologyChange> changes) throws OWLOntologyCreationException {
        ChangeToAxiomConverter converter = new ChangeToAxiomConverter();
        for (OWLOntology ontology : ontologies) {
            converter.addRevisionInfo(ontology, getClientRevision(ontology));
        }
        for (OWLOntologyChange change : changes) {
            change.accept(converter);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Attempting to commit: " + change);
            }
        }
        return converter.getMetaOntology();
	}
	
	private void handleRemoteChanges(OWLOntology changeOntology) throws RemoteQueryException {
	    ChangeUtilities.logOntology("Receiving ontology describing commit results:", changeOntology, LOGGER, Level.DEBUG);
        final ChangeAndRevisionSummary changeSummary = ChangeAndRevisionSummary.getChanges(getOntologies(), changeOntology);
        
        Callable<Boolean> call = new Callable<Boolean>() {
            @Override
            public Boolean call() throws RemoteQueryException {
                try {
                    setUpdateFromServer(true);
                    applyChanges(changeSummary);
                    return true;
                }
                finally {
                    setUpdateFromServer(false);
                }
            }
        };
        try {
            getOntologyManager().callWithWriteLock(call);
        }
        catch (RemoteQueryException rqe) {
            throw rqe;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
