package org.protege.editor.owl.server.change;

import org.protege.editor.owl.server.versioning.ChangeHistoryUtils;
import org.protege.editor.owl.server.versioning.InvalidHistoryFileException;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.HistoryFile;

import org.apache.commons.io.FileUtils;
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

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * @author Timothy Redmond (tredmond) <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ChangeDocumentPoolEntry {

    private Logger logger = LoggerFactory.getLogger(ChangeDocumentPoolEntry.class);

    private HistoryFile historyFile;

    private Future<ChangeHistory> readTask;

    private long lastTouch;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Change History I/O Thread <" + historyFile.getName() + ">");
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

    protected void doWrite(ChangeHistory changes) {
        touch();
        executor.submit(new WriteChanges(changes));
    }

    public ChangeHistory readChangeHistory() throws IOException {
        try {
            doRead();
            return readTask.get();
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

    public void writeChangeHistory(final ChangeHistory changeHistory) {
        doWrite(changeHistory);
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
            logger.info("Reading change history");
            try {
                long startTime = System.currentTimeMillis();
                ChangeHistory result = ChangeHistoryUtils.readChanges(historyFile);
                long interval = System.currentTimeMillis() - startTime;
                logger.info("... success in " + (interval/1000) + " seconds");
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

    private class WriteChanges implements Callable<Boolean> {

        private ChangeHistory changeHistory;

        public WriteChanges(ChangeHistory changeHistory) {
            this.changeHistory = changeHistory;
        }

        @Override
        public Boolean call() {
            logger.info("Writing change history");
            try {
                long startTime = System.currentTimeMillis();
                ChangeHistoryUtils.writeChanges(changeHistory, historyFile);
                long interval = System.currentTimeMillis() - startTime;
                logger.info("... success in " + (interval / 1000) + " seconds.");
                createBackup(historyFile);
                return true;
            }
            catch (Throwable t) {
                logger.error("Exception caught while writing history file", t);
                return false;
            }
        }

        private void createBackup(File historyFile) throws InvalidHistoryFileException, IOException {
            if (logger.isDebugEnabled()) {
                logger.debug("... backup was created");
            }
            HistoryFile backup = getBackupHistoryFile(historyFile);
            FileUtils.copyFile(historyFile, backup);
        }
    }

    private HistoryFile getBackupHistoryFile(File historyFile) throws InvalidHistoryFileException, IOException {
        String path = historyFile.getAbsolutePath();
        String newPath = new StringBuilder(path).insert(path.lastIndexOf(File.separator) + 1, "~").toString();
        return HistoryFile.createNew(newPath);
    }
}
