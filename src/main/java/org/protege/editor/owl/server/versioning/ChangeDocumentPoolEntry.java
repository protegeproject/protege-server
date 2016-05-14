package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.api.exception.OWLServerException;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.HistoryFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class ChangeDocumentPoolEntry {

    private Logger logger = LoggerFactory.getLogger(ChangeDocumentPoolEntry.class.getCanonicalName());

    private ChangeHistory changeHistory;
    private Future<ChangeHistory> readChangeDocumentTask;
    private HistoryFile historyFile;

    private long lastTouch;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Change Document Updates for " + historyFile);
            return thread;
        }
    });

    public ChangeDocumentPoolEntry(@Nonnull HistoryFile historyFile) {
        this.historyFile = historyFile;
        touch();
        readChangeDocumentTask = executor.submit(new ReadChangeDocument());
    }

    public ChangeDocumentPoolEntry(@Nonnull HistoryFile historyFile, ChangeHistory changes) {
        this.historyFile = historyFile;
        this.changeHistory = changes;
        touch();
        executor.submit(new WriteChanges(changes));
    }

    public ChangeHistory getChangeDocument() throws OWLServerException {
        touch();
        if (changeHistory == null) {
            try {
                changeHistory = readChangeDocumentTask.get();
            }
            catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
            catch (ExecutionException ee) {
                if (ee.getCause() instanceof OWLServerException) {
                    throw (OWLServerException) ee.getCause();
                }
                else {
                    throw new RuntimeException(ee);
                }
            }
        }
        return changeHistory;
    }

    public void setChangeDocument(final ChangeHistory newChangeDocument) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting change document for " + historyFile + " to change doc ending at revision " + newChangeDocument.getHeadRevision());
        }
        touch();
        this.changeHistory = newChangeDocument;
        executor.submit(new WriteChanges(newChangeDocument));
    }

    public long getLastTouch() {
        return lastTouch;
    }

    public void dispose() {
        executor.shutdown();
        sync();
    }

    public void sync() {
        try {
            executor.awaitTermination(60, TimeUnit.MINUTES);
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
            HistoryFile backup = getBackupHistoryFile(historyFile);
            try {
                return ChangeHistoryUtils.readChanges(historyFile);
            }
            catch (RuntimeException e) {
                if (backup.exists()) {
                    return ChangeHistoryUtils.readChanges(backup);
                }
                throw e;
            }
            catch (Exception e) {
                if (backup.exists()) {
                    return ChangeHistoryUtils.readChanges(backup);
                }
                throw e;
            }
        }
    }

    private class WriteChanges implements Callable<Boolean> {

        private ChangeHistory newChangeDocument;

        public WriteChanges(ChangeHistory newChangeDocument) {
            this.newChangeDocument = newChangeDocument;
            if (logger.isDebugEnabled()) {
                logger.debug("Created writer for " + historyFile + " and change document ending at " + newChangeDocument.getHeadRevision());
            }
        }

        @Override
        public Boolean call() {
            try {
                if (changeHistory == newChangeDocument) {
                    prepareToSave(historyFile);
                    long startTime = System.currentTimeMillis();
                    ChangeHistoryUtils.writeChanges(newChangeDocument, historyFile);
                    long interval = System.currentTimeMillis() - startTime;
                    if (interval > 1000) {
                        logger.info("Save of " + historyFile + " took " + (interval / 1000) + " seconds.");
                    }
                    else if (logger.isDebugEnabled()) {
                        logger.debug("Wrote new " + historyFile);
                    }
                }
                else if (logger.isDebugEnabled()) {
                    logger.debug("This is not the latest change document");
                    logger.debug("Was supposed to save doc with end revision " + newChangeDocument.getHeadRevision());
                    logger.debug("But now have new save doc with end revision " + changeHistory.getHeadRevision());
                }
                return true;
            }
            catch (Throwable t) {
                logger.error("Exception caught writing history file", t);
                return false;
            }
        }

        private void prepareToSave(File historyFile) throws InvalidHistoryFileException, IOException {
            if (logger.isDebugEnabled()) {
                logger.debug("Preparing backup for " + historyFile);
            }
            HistoryFile backup = getBackupHistoryFile(historyFile);
            if (historyFile.exists() && backup.exists()) {
                backup.delete();
                if (logger.isDebugEnabled()) {
                    logger.debug("Old backup removed");
                }
            }
            if (historyFile.exists()) {
                historyFile.renameTo(backup);
                if (logger.isDebugEnabled()) {
                    logger.debug("Moved " + historyFile + " to " + backup);
                }
            }
        }
    }

    private HistoryFile getBackupHistoryFile(File historyFile) throws InvalidHistoryFileException {
        String path = historyFile.getAbsolutePath();
        String newPath = new StringBuilder(path).insert(path.lastIndexOf(File.separator), "~").toString();
        return HistoryFile.createNew(newPath);
    }
}
