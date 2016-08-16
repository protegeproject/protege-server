package org.protege.editor.owl.server.change;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.protege.editor.owl.server.versioning.ChangeHistoryUtils;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * @author Timothy Redmond (tredmond) <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ChangeDocumentPoolEntry {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDocumentPoolEntry.class);

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private final HistoryFile historyFile;

    private ChangeHistory cachedChangeHistory;

    private long lastTouch;

    private boolean isCached = false;

    public ChangeDocumentPoolEntry(@Nonnull HistoryFile historyFile) {
        this.historyFile = historyFile;
    }

    private void doRead() throws IOException {
        touch();
        logger.info("Reading change history from " + historyFile.getName());
        try {
            long startTime = System.currentTimeMillis();
            ChangeHistory result = ChangeHistoryUtils.readChanges(historyFile);
            long interval = System.currentTimeMillis() - startTime;
            logger.info("... success in " + (interval/1000.0) + " seconds");
            cachedChangeHistory = result;
            isCached = true;
        }
        catch (RuntimeException e) {
            logger.error("Exception caught while reading history file", e);
            readBackupHistory();
        }
        catch (Exception e) {
            logger.error("Exception caught while reading history file", e);
            readBackupHistory();
        }
    }

    private void readBackupHistory() throws IOException {
        final HistoryFile backup = getBackupHistoryFile(historyFile);
        if (backup.exists()) {
            Path backupPath = Paths.get(backup.getAbsolutePath());
            BasicFileAttributes attr = Files.readAttributes(backupPath, BasicFileAttributes.class);
            logger.info(String.format("Unable to fetch the change history from the original history"
                    + "file. Use the backup instead: %s (last modified on %s)",
                    backup.getName(), timeFormat.format(attr.lastModifiedTime())));
            
            // Trying to read the backup file
            long startTime = System.currentTimeMillis();
            cachedChangeHistory = ChangeHistoryUtils.readChanges(backup);
            long interval = System.currentTimeMillis() - startTime;
            logger.info("... success in " + (interval/1000.0) + " seconds");
            
            // Replace the original history file with the backup
            logger.info("Restoring the change history using the backup");
            restoreBackup(backup);
            logger.info("... success");
        }
    }

    private boolean doAppend(ChangeHistory changes) {
        touch();
        if (!changes.isEmpty()) {
            logger.info("Writing changes to " + historyFile.getName());
            logger.info("... " + changes.toString());
            try {
                long startTime = System.currentTimeMillis();
                ChangeHistoryUtils.appendChanges(changes, historyFile);
                long interval = System.currentTimeMillis() - startTime;
                logger.info("... success in " + (interval / 1000.0) + " seconds.");
                isCached = false;
                createBackup(historyFile);
            }
            catch (IOException e) {
                logger.error("Exception caught while writing history file", e);
                return false;
            }
        }
        return true;
    }

    public ChangeHistory getChangeHistory() throws IOException {
        if (!isCached) {
            doRead();
        }
        return cachedChangeHistory;
    }
    
    public DocumentRevision getHead() throws IOException {
        return getChangeHistory().getHeadRevision();
    }

    public void appendChanges(final ChangeHistory changes) {
        doAppend(changes);
    }

    public long getLastTouch() {
        return lastTouch;
    }

    private void touch() {
        this.lastTouch = System.currentTimeMillis();
    }

    private void createBackup(File historyFile) throws IOException {
        HistoryFile backup = getBackupHistoryFile(historyFile);
        FileUtils.copyFile(historyFile, backup);
    }

    private void restoreBackup(File backupFile) throws IOException {
        FileUtils.copyFile(backupFile, historyFile);
    }

    private HistoryFile getBackupHistoryFile(File historyFile) throws IOException {
        String path = historyFile.getAbsolutePath();
        String newPath = new StringBuilder(path).insert(path.lastIndexOf(File.separator) + 1, "~").toString();
        return HistoryFile.createNew(newPath);
    }
}
