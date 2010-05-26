package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.protege.owl.server.api.ClientConnection;
import org.protege.owl.server.api.ServerOntologyInfo;
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
    private Map<IRI, ServerOntologyInfo> serverOntologyInfoByIRI;
    private Map<String, ServerOntologyInfo> serverOntologyInfoByShortName;
    
    //ToDo Fix Me!
    private boolean updateFromServer = false;
    
    private OWLOntologyChangeListener uncommittedChangesListener = new OWLOntologyChangeListener() {

        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
            // ToDo proper threading please...
            if (isUpdateFromServer()) {
                return;
            }
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
    
    protected ServerOntologyInfo getServerOntologyInfo(IRI ontologyName)  throws RemoteQueryException {
        return getOntologyInfoByIRI(false).get(ontologyName);
    }
    
    protected List<OWLOntologyChange> getUncommittedChanges(Set<OWLOntology> ontologies) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLOntology ontology : ontologies) {
            changes.addAll(ontologyInfoMap.get(ontology).getChanges());
        }
        return changes;
    }

    protected void clearUncommittedChanges(Set<OWLOntology> ontologies) {
        for (OWLOntology ontology : ontologies) {
            ClientOntologyInfo info = ontologyInfoMap.get(ontology);
            if (info == null) {
                throw new IllegalArgumentException("Ontology not installed on client: " + ontology);
            }
            info.clearChanges();
        }
    }
    
    protected void addOntology(OWLOntology ontology, String shortName, int revision) {
        ontologyInfoMap.put(ontology, new ClientOntologyInfo(shortName, revision));
    }
    
    protected void removeOntology(OWLOntology ontology) {
        ontologyInfoMap.remove(ontology);
    }
    
    private void makeOntologyInfoMaps() throws RemoteQueryException {
        serverOntologyInfoByIRI = new HashMap<IRI, ServerOntologyInfo>();
        serverOntologyInfoByShortName = new HashMap<String, ServerOntologyInfo>();
        Set<ServerOntologyInfo> infoSet = updateRemoteOntologyList();
        for (ServerOntologyInfo info : infoSet) {
            serverOntologyInfoByIRI.put(info.getOntologyName(), info);
            serverOntologyInfoByShortName.put(info.getShortName(), info);
        }
    }
    
    protected void applyChanges(ChangeAndRevisionSummary changeSummary) throws RemoteQueryException {
        manager.applyChanges(changeSummary.getChanges());
        for (Entry<IRI, Integer> entry : changeSummary.getRevisions().entrySet()) {
            IRI ontologyName = entry.getKey();
            int revision = entry.getValue();
            String shortName = getServerOntologyInfo(ontologyName).getShortName();
            addOntology(getOntologyManager().getOntology(ontologyName), shortName, revision);
        }
    }
    
    protected void setUpdateFromServer(boolean updateFromServer)  {
    	this.updateFromServer = updateFromServer;
    }
    
    public boolean isUpdateFromServer() {
    	return updateFromServer;
    }
    
    
    /* *****************************************************************************
     * Abstract methods.
     */  
    
    protected abstract Set<ServerOntologyInfo> updateRemoteOntologyList() throws RemoteQueryException;

    protected abstract OWLOntology pullMarked(IRI ontologyName, String shortName, int revisionToGet) throws OWLOntologyCreationException, RemoteQueryException;

    protected abstract ChangeAndRevisionSummary getChangesFromServer(OWLOntology ontology, String shortName, int start, int end) throws RemoteQueryException;
    
    /* *****************************************************************************
     * Interface implementations.
     */
    @Override
    public OWLOntologyManager getOntologyManager() {
        return manager;
    }
   
    @Override
    public Map<IRI, ServerOntologyInfo> getOntologyInfoByIRI(boolean forceUpdate) throws RemoteQueryException {
        if (forceUpdate || serverOntologyInfoByIRI == null) {
            makeOntologyInfoMaps();
        }
        return Collections.unmodifiableMap(serverOntologyInfoByIRI);
    }
    
    @Override
    public Map<String, ServerOntologyInfo> getOntologyInfoByShortName(boolean forceUpdate) throws RemoteQueryException {
        if (forceUpdate || serverOntologyInfoByIRI == null) {
            makeOntologyInfoMaps();
        }
        return Collections.unmodifiableMap(serverOntologyInfoByShortName);
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
        ServerOntologyInfo revisions = getServerOntologyInfo(ontologyName);
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
        addOntology(ontology, shortName, closestRevision);
        ChangeAndRevisionSummary changeSummary = getChangesFromServer(ontology, shortName, closestRevision, revisionToGet);
        applyChanges(changeSummary);
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
            getOntologyInfoByIRI(true);
            revision = getServerOntologyInfo(ontologyName).getMaxRevision();
        }
        setUpdateFromServer(true);
        try {
            applyChanges(getChangesFromServer(ontology, clientOntologyInfo.getShortName(), currentRevision, revision));
        }
        catch (RemoteOntologyException e) {
            throw new UpdateFailedException(e);
        }
        finally {
            setUpdateFromServer(false);
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
