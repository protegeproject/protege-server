package org.protege.owl.server.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.semanticweb.owlapi.model.IRI;

public class ChangeDocumentPool {
    private Logger logger = Logger.getLogger(ChangeDocumentPool.class.getCanonicalName());
    private DocumentFactory docFactory;
    private final long timeout;
    private Map<RemoteOntologyDocument, ChangeDocumentPoolEntry> pool = new TreeMap<RemoteOntologyDocument, ChangeDocumentPoolEntry>();
    
    public ChangeDocumentPool(DocumentFactory docFactory, long timeout) {
        this.docFactory = docFactory;
        this.timeout = timeout;
        createTimeoutThread();
    }
    
    private void createTimeoutThread() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread th = new Thread(r, "Change Document Cleanup Detail");
                th.setDaemon(false);
                return th;
            }
        });
        executorService.scheduleAtFixedRate(new Runnable() {
            
            @Override
            public void run() {
                for (Entry<RemoteOntologyDocument, ChangeDocumentPoolEntry> entry : pool.entrySet()) {
                    RemoteOntologyDocument doc = entry.getKey();
                    ChangeDocumentPoolEntry poolEntry = entry.getValue();
                    synchronized (pool) {
                        long now = System.currentTimeMillis();
                        if (poolEntry.getLastTouch() + timeout < now) {
                            poolEntry.dispose();
                            pool.remove(doc);
                            logger.info("Disposed in-memory change history for " + doc);
                        }
                    }
                }
            }
        }, timeout, timeout, TimeUnit.MILLISECONDS);
    }
    
    public ChangeDocument getChangeDocument(RemoteOntologyDocument doc, File historyFile) throws IOException {
        ChangeDocumentPoolEntry entry;
        synchronized (pool) {
            entry = pool.get(doc);
            if (entry == null) {
                entry = new ChangeDocumentPoolEntry(docFactory, historyFile);
                pool.put(doc, entry);
            }
        }
        return entry.getChangeDocument();
    }
    
    public void setChangeDocument(RemoteOntologyDocument doc, File historyFile, ChangeDocument changes) {
        synchronized (pool) {
            ChangeDocumentPoolEntry entry = pool.get(doc);
            if (entry != null) {
                entry.setChangeDocument(changes);
            }
            else {
                entry = new ChangeDocumentPoolEntry(docFactory, historyFile, changes);
                pool.put(doc, entry);
            }
        }
    }
    
    public boolean testServerLocation(IRI serverLocation) {
        synchronized (pool) {
            return pool.containsKey(new RemoteOntologyDocumentImpl(serverLocation));
        }
    }
    
    public void dispose() {
        synchronized (pool) {
            for (ChangeDocumentPoolEntry entry : pool.values()) {
                entry.dispose();
            }
            pool.clear();
        }
    }
    
    public void sync() {
        List<ChangeDocumentPoolEntry> poolEntries;
        synchronized (pool) { 
            poolEntries = new ArrayList<ChangeDocumentPoolEntry>(pool.values());
        }
        for (ChangeDocumentPoolEntry poolEntry : poolEntries) {
            poolEntry.sync();
        }
    }

}
