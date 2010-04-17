package org.protege.owl.server.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.protege.owl.server.api.ClientConnection;
import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.exception.RemoteOntologyException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owl.server.exception.UpdateFailedException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public abstract class AbstractClientConnection implements ClientConnection {
    private OWLOntologyManager manager;
	private Map<OWLOntology, ClientOntologyInfo> ontologyInfoMap = new HashMap<OWLOntology, ClientOntologyInfo>();
    private Set<RemoteOntologyRevisions> revisions;

    
    private OWLOntologyChangeListener uncommittedChangesListener = new OWLOntologyChangeListener() {

        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
        	for (OWLOntologyChange change : changes) {
        		OWLOntology ontology = change.getOntology();
        		ClientOntologyInfo info = ontologyInfoMap.get(ontology);
        		if (info != null) {
        			info.addChange(change);
        		}
        	}
        }
        
    };
    
    protected AbstractClientConnection(OWLOntologyManager manager) {
        this.manager = manager;
        manager.addOntologyChangeListener(uncommittedChangesListener);
    }
    
    protected RemoteOntologyRevisions getRevisionInfo(IRI ontologyName)  throws RemoteQueryException {
        Set<RemoteOntologyRevisions> ontologyList = getRemoteOntologyList(false);
        RemoteOntologyRevisions versions = null;
        for (RemoteOntologyRevisions tryMe : ontologyList) {
            if (tryMe.getOntologyName().equals(ontologyName)) {
                versions = tryMe;
                ontologyName = versions.getOntologyName();
                break;
            }
        }
        return versions;
    }
    
    protected void clearUncommittedChanges(OWLOntology ontology) {
        ClientOntologyInfo info = ontologyInfoMap.get(ontology);
        if (info == null) {
            throw new IllegalArgumentException("Ontology not installed on client: " + ontology);
        }
        info.clearChanges();
    }
    
    protected void addOntology(OWLOntology ontology, String shortName, int revision) {
        ontologyInfoMap.put(ontology, new ClientOntologyInfo(shortName, revision));
    }
    
    protected void removeOntology(OWLOntology ontology) {
        ontologyInfoMap.remove(ontology);
    }
    
    
    /* *****************************************************************************
     * Abstract methods.
     */  
    
    protected abstract Set<RemoteOntologyRevisions> updateRemoteOntologyList() throws RemoteQueryException;

    protected abstract OWLOntology pullMarked(IRI ontologyName, String shortName, int revisionToGet) throws OWLOntologyCreationException, RemoteQueryException;

    protected abstract List<OWLOntologyChange> getChangesFromServer(OWLOntology ontology, String shortName, int start, int end) throws RemoteQueryException;
    
    /* *****************************************************************************
     * Interface implementations.
     */
    @Override
    public OWLOntologyManager getOntologyManager() {
        return manager;
    }
    
    @Override
    public Set<RemoteOntologyRevisions> getRemoteOntologyList(boolean forceUpdate) throws RemoteQueryException {
        if (forceUpdate || revisions == null) {
            revisions = updateRemoteOntologyList();
        }
        return revisions;
    }
    
    @Override
    public Set<OWLOntology> getOntologies() {
    	return ontologyInfoMap.keySet();
    }
    
    @Override
    public int getRevision(OWLOntology ontology) {
        ClientOntologyInfo info = ontologyInfoMap.get(ontology);
        return info.getRevision();
    }

    public OWLOntology pull(IRI ontologyName, Integer revisionToGet) throws OWLOntologyCreationException, RemoteQueryException {
        RemoteOntologyRevisions revisions = getRevisionInfo(ontologyName);
        if (revisions == null) {
            return null;
        }
        if (revisionToGet == null) {
            revisionToGet = revisions.getMaxRevision();
        }
        Integer closestRevision = revisions.getLatestMarkedRevision(revisionToGet);
        if (closestRevision == null) {
            return null;
        }
        String shortName = revisions.getShortName();
        OWLOntology ontology = pullMarked(ontologyName, shortName, closestRevision);
        manager.applyChanges(getChangesFromServer(ontology, shortName, closestRevision, revisionToGet));
        addOntology(ontology, shortName, revisionToGet);
        return ontology;
    }

    @Override
    public void update(OWLOntology ontology, Integer revision) throws OWLOntologyChangeException, RemoteQueryException {
        ClientOntologyInfo clientOntologyInfo = ontologyInfoMap.get(ontology);
        Integer currentRevision = clientOntologyInfo.getRevision();
        if (currentRevision == null) {
            return;
        }
        if (revision == null) {
            IRI ontologyName = ontology.getOntologyID().getOntologyIRI();
            revision = getRevisionInfo(ontologyName).getMaxRevision();
        }
        try {
            getOntologyManager().applyChanges(getChangesFromServer(ontology, clientOntologyInfo.getShortName(), currentRevision, revision));
            ontologyInfoMap.get(ontology).setRevision(revision);
        }
        catch (RemoteOntologyException e) {
            throw new UpdateFailedException(e);
        }
    }

    @Override
    public List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology) {
        return ontologyInfoMap.get(ontology).getChanges();
    }
    
    @Override
    public void dispose() {
        manager.removeOntologyChangeListener(uncommittedChangesListener);
    }
    
    
    
}
