package org.protege.editor.owl.server.change;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

public class ChangeDocumentPool {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDocumentPool.class);

    private static final int DEFAULT_POOL_TIMEOUT = 60 * 1000;

    private final ScheduledExecutorService executorService;

    private final Map<HistoryFile, ChangeDocumentPoolEntry> pool = new TreeMap<>();

    private int consecutiveCleanupFailures = 0;

    public ChangeDocumentPool() {
        this(DEFAULT_POOL_TIMEOUT);
    }

    public ChangeDocumentPool(long timeout) {
        executorService = createExecutorService(timeout);
    }

    private ScheduledExecutorService createExecutorService(long timeout) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread th = new Thread(r, "Change Document Pool Maintainer Thread");
                        th.setDaemon(false);
                        return th;
                    }
                });
        executorService.scheduleAtFixedRate(new Runnable() { // Setup a cleanup timer
            @Override
            public void run() {
                try {
                    for (Entry<HistoryFile, ChangeDocumentPoolEntry> entry : new HashSet<>(pool.entrySet())) {
                        File f = entry.getKey();
                        ChangeDocumentPoolEntry poolEntry = entry.getValue();
                        synchronized (pool) {
                            long now = System.currentTimeMillis();
                            if (poolEntry.getLastTouch() + timeout < now) {
                                poolEntry.dispose();
                                pool.remove(f);
                                logger.info("Disposed in-memory change history for " + f.getName());
                            }
                        }
                    }
                    consecutiveCleanupFailures = 0;
                }
                catch (Error e) {
                    logger.error("Exception caught cleaning open ontology pool.", e);
                    consecutiveCleanupFailures++;
                }
                catch (RuntimeException e) {
                    logger.error("Exception caught cleaning open ontology pool.", e);
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
        return executorService;
    }

    public ChangeHistory lookup(HistoryFile historyFile) throws IOException {
        ChangeDocumentPoolEntry entry;
        synchronized (pool) {
            entry = pool.get(historyFile);
            if (entry == null) {
                entry = new ChangeDocumentPoolEntry(historyFile);
                pool.put(historyFile, entry);
                logger.info("Checked out in-memory change history for " + historyFile.getName());
            }
        }
        return entry.readChangeHistory();
    }
    
    public DocumentRevision lookupHead(HistoryFile historyFile) throws IOException {
        ChangeDocumentPoolEntry entry;
        synchronized (pool) {
            entry = pool.get(historyFile);
            if (entry == null) {
                entry = new ChangeDocumentPoolEntry(historyFile);
                pool.put(historyFile, entry);
                logger.info("Checked out in-memory change history for " + historyFile.getName());
            }
        }
        return entry.getHead();
    }

    public void update(HistoryFile historyFile, ChangeHistory changeHistory) {
        synchronized (pool) {
            ChangeDocumentPoolEntry entry = pool.get(historyFile);
            if (entry == null) {
                entry = new ChangeDocumentPoolEntry(historyFile);
                entry.appendChangeHistory(changeHistory);
                pool.put(historyFile, entry);
            }
            else {
                entry.appendChangeHistory(changeHistory);
            }
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
