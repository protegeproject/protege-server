package org.protege.editor.owl.server.change;

import org.protege.editor.owl.server.versioning.ChangeHistoryUtils;
import org.protege.editor.owl.server.versioning.InvalidHistoryFileException;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * @author Timothy Redmond (tredmond) <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ChangeDocumentPoolEntry {

    private Logger logger = LoggerFactory.getLogger(ChangeDocumentPoolEntry.class);

    private static int counter = 0;

    private HistoryFile historyFile;

    private Future<ChangeHistory> readTask;

    private ChangeHistory cacheHistory;

    private long lastTouch;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Change History I/O Thread " + counter++);
            return thread;
        }
    });

    public ChangeDocumentPoolEntry(@Nonnull HistoryFile historyFile) {
        this.historyFile = historyFile;
    }

    protected void doRead() {
        touch();
        readTask = executor.submit(new ReadChangeDocument());
    }

    protected void doAppend(ChangeHistory changes) {
        touch();
        executor.submit(new AppendChanges(changes));
    }

    public ChangeHistory readChangeHistory() throws IOException {
        try {
            if (cacheHistory == null) {
                doRead();
                cacheHistory = readTask.get();
            }
            return cacheHistory;
        }
        catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
        catch (ExecutionException ee) {
            if (ee.getCause() instanceof IOException) {
                throw (IOException) ee.getCause();
            }
            else {
                throw new RuntimeException(ee);
            }
        }
    }

    public void appendChangeHistory(final ChangeHistory changeHistory) {
        doAppend(changeHistory);
    }

    public long getLastTouch() {
        return lastTouch;
    }

    public void dispose() {
        cacheHistory = null;
        executor.shutdown();
        sync();
    }

    public void sync() {
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        }
        catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    private void touch() {
        this.lastTouch = System.currentTimeMillis();
    }

    private class ReadChangeDocument implements Callable<ChangeHistory> {

        @Override
        public ChangeHistory call() throws Exception {
            logger.info("Reading change history");
            try {
                long startTime = System.currentTimeMillis();
                ChangeHistory result = ChangeHistoryUtils.readChanges(historyFile);
                long interval = System.currentTimeMillis() - startTime;
                logger.debug("... success in " + (interval/1000) + " seconds");
                return result;
            }
            catch (RuntimeException e) {
                HistoryFile backup = getBackupHistoryFile(historyFile);
                if (backup.exists()) {
                    return ChangeHistoryUtils.readChanges(backup);
                }
                throw e;
            }
            catch (Exception e) {
                HistoryFile backup = getBackupHistoryFile(historyFile);
                if (backup.exists()) {
                    return ChangeHistoryUtils.readChanges(backup);
                }
                throw e;
            }
        }
    }

    private class AppendChanges implements Callable<Boolean> {

        private ChangeHistory incomingChanges;

        public AppendChanges(ChangeHistory changeHistory) {
            this.incomingChanges = changeHistory;
        }

        @Override
        public Boolean call() {
            if (!incomingChanges.isEmpty()) {
                logger.info("Writing change history into " + historyFile + "\n" + incomingChanges.toString());
                try {
                    long startTime = System.currentTimeMillis();
                    ChangeHistoryUtils.appendChanges(incomingChanges, historyFile);
                    long interval = System.currentTimeMillis() - startTime;
                    logger.debug("... success in " + (interval / 1000) + " seconds.");
                    createBackup(historyFile);
                    updateCache();
                }
                catch (Throwable t) {
                    logger.error("Exception caught while writing history file", t);
                    return false;
                }
            }
            return true;
        }

        private void createBackup(File historyFile) throws InvalidHistoryFileException, IOException {
            if (logger.isDebugEnabled()) {
                logger.debug("... backup was created");
            }
            HistoryFile backup = getBackupHistoryFile(historyFile);
            FileUtils.copyFile(historyFile, backup);
        }

        private void updateCache() {
            if (cacheHistory != null) {
                final DocumentRevision base = incomingChanges.getBaseRevision();
                final DocumentRevision end = incomingChanges.getHeadRevision();
                for (DocumentRevision current = base.next(); current.behindOrSameAs(end); current = current.next()) {
                    RevisionMetadata metadata = incomingChanges.getMetadataForRevision(current);
                    List<OWLOntologyChange> changes = incomingChanges.getChangesForRevision(current);
                    cacheHistory.addRevision(metadata, changes);
                }
            }
        }
    }

    private HistoryFile getBackupHistoryFile(File historyFile) throws InvalidHistoryFileException, IOException {
        String path = historyFile.getAbsolutePath();
        String newPath = new StringBuilder(path).insert(path.lastIndexOf(File.separator) + 1, "~").toString();
        return HistoryFile.createNew(newPath);
    }
}
