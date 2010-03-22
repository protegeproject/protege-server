package org.protege.owl.server.connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.exception.RemoteOntologyChangeException;
import org.protege.owl.server.exception.RemoteOntologyCreationException;
import org.protege.owl.server.exception.RemoteOntologyException;
import org.protege.owl.server.exception.UpdateFailedException;
import org.protege.owl.server.util.AbstractClientConnection;
import org.protege.owlapi.apibinding.ProtegeOWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class LocalClientConnection extends AbstractClientConnection {
    private Server server;
    private Map<IRI, Integer> localVersions = new HashMap<IRI,Integer>();
    
    public LocalClientConnection(Server server) {
        super(ProtegeOWLManager.createOWLOntologyManager());
        this.server = server;
    }

    @Override
    public void commit(OWLOntology ontology) throws RemoteOntologyChangeException {
        IRI ontologyName = ontology.getOntologyID().getOntologyIRI();
        if (!localVersions.containsKey(ontologyName)) {
            throw new IllegalStateException("Ontology should be contained on client");
        }
        server.applyChanges(localVersions, getUncommittedChanges(ontology));
        clearUncommittedChanges(ontology);
    }

    @Override
    public Map<IRI, RemoteOntologyRevisions> getRemoteOntologyList() {
        return server.getOntologyList();
    }

    @Override
    public int getRevision(OWLOntology ontology) {
        return localVersions.get(ontology.getOntologyID().getOntologyIRI());
    }

    @Override
    public OWLOntology pull(IRI ontologyName, Integer revision) throws RemoteOntologyCreationException {
        Map<IRI, RemoteOntologyRevisions> ontologyList = getRemoteOntologyList();
        RemoteOntologyRevisions versions = ontologyList.get(ontologyName);
        if (versions == null) { 
            return null;
        }
        Integer closestRevision = versions.getLatestMarkedRevision(revision);
        if (closestRevision == null) {
            return null;
        }
        OWLOntology ontology = null; 
        try {
            ontology = getOntologyManager().loadOntologyFromOntologyDocument(server.getOntologyStream(ontologyName, closestRevision));
            getOntologyManager().applyChanges(server.getChanges(ontologyName, closestRevision, revision));
            return ontology;
        }
        catch (RemoteOntologyCreationException e) {
            throw e;
        }
        catch (OWLOntologyCreationException e) {
            throw new RemoteOntologyCreationException(e);
        }
        catch (RemoteOntologyException e) {
            throw new RemoteOntologyCreationException(e);
        }
    }

    @Override
    public void update(OWLOntology ontology, Integer revision) throws OWLOntologyChangeException {
        IRI ontologyName = ontology.getOntologyID().getOntologyIRI();
        Integer currentRevision = localVersions.get(ontologyName);
        if (currentRevision == null) {
            return;
        }
        if (revision == null) {
            Map<IRI, RemoteOntologyRevisions> ontologyList = getRemoteOntologyList();
            RemoteOntologyRevisions revisions = ontologyList.get(ontologyName);
            revision = revisions.getMaxRevision();
        }
        try {
            getOntologyManager().applyChanges(server.getChanges(ontologyName, currentRevision, revision));
        }
        catch (RemoteOntologyException e) {
            throw new UpdateFailedException(e);
        }
    }

    @Override
    public Set<OWLOntology> getOntologies() {
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        for (IRI ontologyName : localVersions.keySet()) {
            ontologies.add(getOntologyManager().getOntology(ontologyName));
        }
        return ontologies;
    }
}
