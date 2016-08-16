package org.protege.editor.owl.server.change;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeDocumentPool {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDocumentPool.class);

    private static final int DEFAULT_POOL_TIMEOUT = 60 * 1000;

    private final long timeout;

    @GuardedBy("this")
    private final Map<HistoryFile, ChangeDocumentPoolEntry> pool = new TreeMap<>();

    private ScheduledExecutorService executorService;

    private int consecutiveCleanupFailures = 0;

    public ChangeDocumentPool() {
        this(DEFAULT_POOL_TIMEOUT);
    }

    public ChangeDocumentPool(long timeout) {
        this.timeout = timeout;
        setExecutorService(createExecutorService());
    }

    private void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    private ScheduledExecutorService createExecutorService() {
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
                    synchronized (this) {
                        for (Entry<HistoryFile, ChangeDocumentPoolEntry> entry : new HashSet<>(pool.entrySet())) {
                            File historyFile = entry.getKey();
                            ChangeDocumentPoolEntry poolEntry = entry.getValue();
                            long now = System.currentTimeMillis();
                            if ((poolEntry.getLastTouch() + timeout) < now) {
                                pool.remove(historyFile);
                                logger.info("Disposed in-memory change history for " + historyFile.getName());
                            }
                        }
                        consecutiveCleanupFailures = 0; // reset the counter
                    }
                }
                catch (Error e) {
                    logger.error("Exception caught while cleaning change document pool.", e);
                    consecutiveCleanupFailures++;
                }
                catch (RuntimeException e) {
                    logger.error("Exception caught while cleaning change document pool.", e);
                    consecutiveCleanupFailures++;
                }
                finally {
                    if (consecutiveCleanupFailures > 8) {
                        logger.warn("Restarting the maintainer thread. Server could run out of memory");
                        if (!executorService.isShutdown()) {
                            logger.warn("... terminating pending tasks");
                            List<Runnable> pendingTasks = executorService.shutdownNow();
                            for (Runnable task : pendingTasks) {
                                logger.warn("... kill " + task.toString() + " prematurely");
                            }
                        }
                        setExecutorService(createExecutorService());
                    }
                }
            }
        }, timeout, timeout, TimeUnit.MILLISECONDS);
        return executorService;
    }

    public ChangeHistory lookup(HistoryFile historyFile) throws IOException {
        synchronized (this) {
            ChangeDocumentPoolEntry entry = pool.get(historyFile);
            if (entry == null) {
                entry = new ChangeDocumentPoolEntry(historyFile);
                pool.put(historyFile, entry);
            }
            return entry.getChangeHistory();
        }
    }
    
    public DocumentRevision lookupHead(HistoryFile historyFile) throws IOException {
        synchronized (this) {
            ChangeDocumentPoolEntry entry = pool.get(historyFile);
            if (entry == null) {
                entry = new ChangeDocumentPoolEntry(historyFile);
                pool.put(historyFile, entry);
            }
            return entry.getHead();
        }
    }

    public void appendChanges(HistoryFile historyFile, ChangeHistory changes) {
        synchronized (this) {
            ChangeDocumentPoolEntry entry = pool.get(historyFile);
            if (entry == null) {
                entry = new ChangeDocumentPoolEntry(historyFile);
                pool.put(historyFile, entry);
            }
            entry.appendChanges(changes);
        }
    }

    public void dispose() {
        synchronized (this) {
            pool.clear();
        }
        executorService.shutdown();
    }
}
