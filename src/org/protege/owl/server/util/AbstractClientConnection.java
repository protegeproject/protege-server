package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.protege.owl.server.api.ClientConnection;
import org.protege.owl.server.api.ServerOntologyInfo;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public abstract class AbstractClientConnection implements ClientConnection {
    public static final Logger LOGGER = Logger.getLogger(AbstractClientConnection.class);
    protected enum State {
        IDLE, UPDATE_IN_PROGRESS, COMMIT_IN_PROGRESS;
    };
    private ProtegeOWLOntologyManager manager;
	protected State state = State.IDLE;
    private Map<OWLOntology, ClientOntologyInfo> ontologyInfoMap = new HashMap<OWLOntology, ClientOntologyInfo>();
    private Map<IRI, ServerOntologyInfo> serverOntologyInfoByIRI;
    private Map<String, ServerOntologyInfo> serverOntologyInfoByShortName;
    private boolean updateFromServer = false;
    
    private OWLOntologyChangeListener uncommittedChangesListener = new OWLOntologyChangeListener() {

        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
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
    
    protected AbstractClientConnection(ProtegeOWLOntologyManager manager) {
        this.manager = manager;
        manager.addOntologyChangeListener(uncommittedChangesListener);
    }
    
    protected synchronized ServerOntologyInfo getServerOntologyInfo(IRI ontologyName)  throws RemoteQueryException {
        return getOntologyInfoByIRI(false).get(ontologyName);
    }
    
    protected synchronized List<OWLOntologyChange> getUncommittedChanges(Set<OWLOntology> ontologies) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLOntology ontology : ontologies) {
            changes.addAll(ontologyInfoMap.get(ontology).getChanges());
        }
        return changes;
    }

    protected synchronized void clearUncommittedChanges(Collection<OWLOntologyChange> changes) {
        for (OWLOntologyChange change : changes) {
            OWLOntology ontology = change.getOntology();
            ClientOntologyInfo info = ontologyInfoMap.get(ontology);
            if (info == null) {
                throw new IllegalArgumentException("Ontology not installed on client: " + ontology);
            }
            info.removeChange(change);
        }
    }
    
    protected synchronized void addOntology(OWLOntology ontology, String shortName, int revision) {
        ontologyInfoMap.put(ontology, new ClientOntologyInfo(shortName, revision));
    }
    
    protected synchronized void removeOntology(OWLOntology ontology) {
        ontologyInfoMap.remove(ontology);
    }
    
    private void makeOntologyInfoMaps() throws RemoteQueryException {
        Set<ServerOntologyInfo> infoSet = getRemoteOntologyList();
        synchronized (this) {
            serverOntologyInfoByIRI = new HashMap<IRI, ServerOntologyInfo>();
            serverOntologyInfoByShortName = new HashMap<String, ServerOntologyInfo>();
            for (ServerOntologyInfo info : infoSet) {
                serverOntologyInfoByIRI.put(info.getOntologyName(), info);
                serverOntologyInfoByShortName.put(info.getShortName(), info);
            }
        }
    }
    
    protected void applyChanges(final ChangeAndRevisionSummary changeSummary) throws RemoteQueryException {
        Callable<Boolean> call = new Callable<Boolean>() {
            @Override
            public Boolean call() throws RemoteQueryException {
                manager.applyChanges(changeSummary.getChanges());
                synchronized (AbstractClientConnection.this) {
                    for (Entry<IRI, Integer> entry : changeSummary.getRevisions().entrySet()) {
                        IRI ontologyName = entry.getKey();
                        OWLOntology ontology = getOntologyManager().getOntology(ontologyName);
                        int revision = entry.getValue();
                        ClientOntologyInfo info = ontologyInfoMap.get(ontology);
                        if (info == null) {
                            String shortName = getServerOntologyInfo(ontologyName).getShortName();
                            addOntology(ontology, shortName, revision);
                        }
                        else {
                            info.setRevision(revision);
                        }
                    }    
                }
                return true;
            }
        };
        try {
            manager.callWithWriteLock(call);
        }
        catch (Exception e) {
            throw convertException(e, RemoteQueryException.class);
        }
    }

    protected void stateChange(State newState) {
        switch (newState) {
        case IDLE:
            synchronized (this) {
                state = State.IDLE;
                this.notifyAll();
            }
            break;
        default:
            synchronized (this) {
                while (state != State.IDLE) {
                    try {
                        this.wait();
                    }
                    catch (InterruptedException ie) {
                        LOGGER.warn("Strange exception while asleep", ie);
                    }
                }
            }
        }
    }

    private <X extends Exception> X convertException(Exception e, Class<? extends X> clazz) {
        if (clazz.isAssignableFrom(e.getClass())) {
            return clazz.cast(e);
        }
        else {
            throw new RuntimeException(e);
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
    
    protected abstract Set<ServerOntologyInfo> getRemoteOntologyList() throws RemoteQueryException;

    protected abstract OWLOntology pullMarked(IRI ontologyName, String shortName, int revisionToGet) throws OWLOntologyCreationException, RemoteQueryException;

    protected abstract ChangeAndRevisionSummary getChangesFromServer(OWLOntology ontology, String shortName, int start, int end) throws RemoteQueryException;
    
    /* *****************************************************************************
     * Interface implementations.
     */
    @Override
    public ProtegeOWLOntologyManager getOntologyManager() {
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

    @Override
    public OWLOntology pull(IRI ontologyName, Integer revisionToGet) throws OWLOntologyCreationException, RemoteQueryException {
        Integer closestRevision;
        String shortName;
        synchronized (this) {
            ServerOntologyInfo revisions = getServerOntologyInfo(ontologyName);
            if (revisions == null) {
                return null;
            }
            if (revisionToGet == null) {
                revisionToGet = revisions.getMaxRevision();
            }
            closestRevision = revisions.getLatestMarkedRevision(revisionToGet);
            if (closestRevision == null) {
                return null;
            }
            shortName = revisions.getShortName();
        }
        OWLOntology ontology = pullMarked(ontologyName, shortName, closestRevision);
        addOntology(ontology, shortName, closestRevision);
        final ChangeAndRevisionSummary changeSummary = getChangesFromServer(ontology, shortName, closestRevision, revisionToGet);
        Callable<Boolean> call = new Callable<Boolean>() {
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
            manager.callWithWriteLock(call);
        }
        catch (Exception e) {
            throw convertException(e, RemoteQueryException.class);
        }
        return ontology;
    }

    @Override
    public void update(OWLOntology ontology, Integer revision) throws OWLOntologyChangeException, RemoteQueryException {
        final ClientOntologyInfo clientOntologyInfo;
        Integer currentRevision;
        stateChange(State.UPDATE_IN_PROGRESS);
        try {
            synchronized (this) {
                clientOntologyInfo = ontologyInfoMap.get(ontology);
                currentRevision = clientOntologyInfo.getRevision();
                if (currentRevision == null) {
                    return;
                }
                if (revision == null) {
                    IRI ontologyName = ontology.getOntologyID().getOntologyIRI();
                    getOntologyInfoByIRI(true);
                    revision = getServerOntologyInfo(ontologyName).getMaxRevision();
                }
            }

            final ChangeAndRevisionSummary serverChanges = getChangesFromServer(ontology, clientOntologyInfo.getShortName(), currentRevision, revision);
            Callable<Boolean> call = new Callable<Boolean>() {
               @Override
                public Boolean call() throws RemoteQueryException {
                   try {
                       setUpdateFromServer(true);
                       applyChanges(serverChanges);
                       synchronized (AbstractClientConnection.this) {
                           List<OWLOntologyChange> pendingChanges = clientOntologyInfo.getChanges();
                           pendingChanges = Utilities.swapOrderOfChangeLists(pendingChanges, serverChanges.getChanges());
                           clientOntologyInfo.setChanges(pendingChanges);
                       }
                       return true;
                   }
                   finally {
                       setUpdateFromServer(false);
                   }
                } 
            };
            try {
                manager.callWithWriteLock(call);
            }
            catch (Exception e) {
                throw convertException(e, RemoteQueryException.class);
            }
        }
        finally {
            stateChange(State.IDLE);
        }
    }

    @Override
    public synchronized List<OWLOntologyChange> getUncommittedChanges(OWLOntology ontology) {
        return ontologyInfoMap.get(ontology).getChanges();
    }
    
    @Override
    public void dispose() {
        manager.removeOntologyChangeListener(uncommittedChangesListener);
    }

}
