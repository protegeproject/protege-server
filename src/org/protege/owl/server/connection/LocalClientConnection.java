package org.protege.owl.server.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owl.server.util.AbstractClientConnection;
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
    protected Set<RemoteOntologyRevisions> updateRemoteOntologyList() throws RemoteQueryException {
        return server.getOntologyList();
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
    protected List<OWLOntologyChange> getChangesFromServer(OWLOntology ontology, String shortName, int start, int end) throws RemoteQueryException {
        try {
            return server.getChanges(ontology.getOntologyID().getOntologyIRI(), start, end);
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
    public void commit(Set<OWLOntology> ontologies) throws RemoteOntologyChangeException {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        Map<IRI, Integer> versionMap = new HashMap<IRI, Integer>();
        for (OWLOntology ontology : ontologies) {
            IRI ontologyName = ontology.getOntologyID().getOntologyIRI();
            changes.addAll(getUncommittedChanges(ontology));
            versionMap.put(ontologyName, getRevision(ontology));
        }
        server.applyChanges(versionMap, changes);
        clearUncommittedChanges(ontologies);
    }   

}
