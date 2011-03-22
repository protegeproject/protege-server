package org.protege.owl.server.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerOntologyInfo;
import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owl.server.util.AbstractClientConnection;
import org.protege.owl.server.util.ChangeAndRevisionSummary;
import org.protege.owlapi.apibinding.ProtegeOWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class LocalClientConnection extends AbstractClientConnection {
    private Server server;
    
    public LocalClientConnection(Server server) {
        super(ProtegeOWLManager.createOWLOntologyManager());
        this.server = server;
    }



    /* *****************************************************************************
     * Abstract methods from AbstractClientConnection.
     */
    
    @Override
    protected Set<ServerOntologyInfo> getRemoteOntologyList() throws RemoteQueryException {
        throw new IllegalStateException("Shouldn't be called");
    }
    
    @Override
    public OWLOntology pullMarked(IRI ontologyName, String shortName, int revision) throws OWLOntologyCreationException, RemoteQueryException  {
        try {
            return getOntologyManager().loadOntologyFromOntologyDocument(server.getOntologyStream(ontologyName, revision));
        }
        catch (IOException ioe) {
            throw new RemoteQueryException(ioe);
        }
    }
    
    @Override
    protected ChangeAndRevisionSummary getChangesFromServer(OWLOntology ontology, String shortName, int start, int end) throws RemoteQueryException {
        try {
            ChangeAndRevisionSummary summary = new ChangeAndRevisionSummary();
            summary.setChanges( server.getChanges(ontology.getOntologyID().getOntologyIRI(), start, end));
            summary.setRevisions(Collections.singletonMap(ontology.getOntologyID().getOntologyIRI(), end));
            return summary;
        }
        catch (RemoteQueryException e) {
            throw e;
        }
        catch (RemoteOntologyException e) {
            throw new RemoteQueryException(e);
        }
    }
    
    /* *****************************************************************************
     * Interface implementations.
     */

    @Override
    public Map<IRI, ServerOntologyInfo> getOntologyInfoByIRI(boolean forceUpdate) throws RemoteQueryException {
        return server.getOntologyInfoByIRI();
    }
    
    @Override
    public Map<String, ServerOntologyInfo> getOntologyInfoByShortName(boolean forceUpdate) throws RemoteQueryException {
        return server.getOntologyInfoByShortName();
    }
    
    @Override
    public void commit(Set<OWLOntology> ontologies) throws RemoteOntologyChangeException {
        stateChange(State.COMMIT_IN_PROGRESS);
        try {
            List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
            Map<IRI, Integer> versionMap = new HashMap<IRI, Integer>();
            for (OWLOntology ontology : ontologies) {
                IRI ontologyName = ontology.getOntologyID().getOntologyIRI();
                changes.addAll(getUncommittedChanges(ontology));
                versionMap.put(ontologyName, getRevision(ontology));
            }
            server.applyChanges(versionMap, changes);
            clearUncommittedChanges(changes);
        }
        finally {
            stateChange(State.IDLE);
        }
    }   

}
