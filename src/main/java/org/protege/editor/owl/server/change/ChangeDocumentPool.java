package org.protege.editor.owl.server.change;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class ChangeDocumentPool {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDocumentPool.class);

    private static final int DEFAULT_MAINTAIN_INTERVAL = 3 * 60 * 1000; // 3 mins
    private static final int DEFAULT_POOL_TIMEOUT = 15 * 2 * 60 * 1000; // 15 mins

    private final Cache<String, ChangeDocumentPoolEntry> pool;

    private final ScheduledExecutorService executorService;

    public ChangeDocumentPool() {
        this(DEFAULT_POOL_TIMEOUT);
    }

    public ChangeDocumentPool(long timeout) {
        pool = CacheBuilder.newBuilder()
                .expireAfterAccess(timeout, TimeUnit.MILLISECONDS)
                .removalListener(new RemovalListener<String, ChangeDocumentPoolEntry>() {
                    public void onRemoval(RemovalNotification<String, ChangeDocumentPoolEntry> notification) {
                        logger.info(String.format("Dispose in-memory cache history for %s", notification.getKey()));
                    }
                }).build();
        executorService = createPoolCleanupThread();
    }

    private ScheduledExecutorService createPoolCleanupThread() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread th = new Thread(r, "Change Document Pool Maintainer Thread");
                        th.setDaemon(false);
                        return th;
                    }
                });
        executorService.scheduleAtFixedRate(() -> {
            if (pool.size() > 0) {
                pool.cleanUp();
            }
        }, DEFAULT_MAINTAIN_INTERVAL, DEFAULT_MAINTAIN_INTERVAL, TimeUnit.MILLISECONDS);
        return executorService;
    }

    public synchronized ChangeHistory lookup(HistoryFile historyFile) throws IOException {
        ChangeDocumentPoolEntry entry = getPoolEntry(historyFile);
        return entry.getChangeHistory();
    }
    
    public synchronized DocumentRevision lookupHead(HistoryFile historyFile) throws IOException {
        ChangeDocumentPoolEntry entry = getPoolEntry(historyFile);
        return entry.getHead();
    }

    public synchronized void appendChanges(HistoryFile historyFile, ChangeHistory changes) {
        ChangeDocumentPoolEntry entry = getPoolEntry(historyFile);
        entry.appendChanges(changes);
    }

    @Nonnull
    private ChangeDocumentPoolEntry getPoolEntry(HistoryFile historyFile) {
        try {
            String historyLocation = historyFile.getAbsolutePath();
            ChangeDocumentPoolEntry entry = pool.get(historyLocation, () -> new ChangeDocumentPoolEntry(historyFile));
            return entry;
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void dispose() {
        pool.invalidateAll();
        executorService.shutdownNow();
    }
}
