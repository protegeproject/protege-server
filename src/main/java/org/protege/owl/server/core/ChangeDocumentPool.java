package org.protege.owl.server.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.ServerOntologyDocument;
import org.protege.owl.server.api.server.ServerPath;

public class ChangeDocumentPool {
    private Logger logger = LoggerFactory.getLogger(ChangeDocumentPool.class.getCanonicalName());
    private ScheduledExecutorService executorService;
    private DocumentFactory docFactory;
    private final long timeout;
    private Map<ServerOntologyDocument, ChangeDocumentPoolEntry> pool = new TreeMap<ServerOntologyDocument, ChangeDocumentPoolEntry>();
    private int consecutiveCleanupFailures = 0;
    
    public ChangeDocumentPool(DocumentFactory docFactory, long timeout) {
        this.docFactory = docFactory;
        this.timeout = timeout;
        createTimeoutThread();
    }
    
    private void createTimeoutThread() {
        executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
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
                try {
                    for (Entry<ServerOntologyDocument, ChangeDocumentPoolEntry> entry : new HashSet<Entry<ServerOntologyDocument, ChangeDocumentPoolEntry>>(pool.entrySet())) {
                        ServerOntologyDocument doc = entry.getKey();
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
                    consecutiveCleanupFailures = 0;
                }
                catch (Error t) {
                    logger.error("Exception caught cleaning open ontology pool.", t);
                    consecutiveCleanupFailures++;
                }
                catch (RuntimeException re) {
                    logger.error("Exception caught cleaning open ontology pool.", re);
                    consecutiveCleanupFailures++;
                }
                finally {
                    if (consecutiveCleanupFailures > 8) {
                        logger.error("Shutting down clean up thread for change history management.");
                        logger.error("Server could run out of memory");
                    }
                }
            }
        }, timeout, timeout, TimeUnit.MILLISECONDS);
    }
    
    public ChangeHistory getChangeDocument(ServerOntologyDocument doc, File historyFile) throws OWLServerException {
        ChangeDocumentPoolEntry entry;
        synchronized (pool) {
            entry = pool.get(doc);
            if (entry == null) {
                entry = new ChangeDocumentPoolEntry(docFactory, historyFile);
                pool.put(doc, entry);
                logger.info("Checked out in-memory change history for " + doc);
            }
        }
        return entry.getChangeDocument();
    }
    
    public void setChangeDocument(ServerOntologyDocument doc, File historyFile, ChangeHistory changes) {
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
    
    public boolean testServerLocation(ServerPath serverPath) {
        synchronized (pool) {
            return pool.containsKey(new ServerOntologyDocumentImpl(serverPath));
        }
    }
    
    public void dispose() {
        synchronized (pool) {
            for (ChangeDocumentPoolEntry entry : pool.values()) {
                entry.dispose();
            }
            pool.clear();
        }
        executorService.shutdown();
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
