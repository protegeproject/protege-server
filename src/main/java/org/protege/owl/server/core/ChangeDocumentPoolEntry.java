package org.protege.owl.server.core;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.ChangeHistoryUtilities;

public class ChangeDocumentPoolEntry {
    
    private Logger logger = Logger.getLogger(ChangeDocumentPoolEntry.class.getCanonicalName());
    private DocumentFactory factory;
    private ChangeHistory changeDocument;
    private Future<ChangeHistory> readChangeDocumentTask;
    private File historyFile;
    private long lastTouch;
    
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
       @Override
        public Thread newThread(Runnable r) {
           Thread thread = new Thread(r, "Change Document Updates for " + historyFile);
           return thread;
        } 
    });
    
    public ChangeDocumentPoolEntry(DocumentFactory factory, final File historyFile) {
        this.factory = factory;
        this.historyFile = historyFile;
        touch();
        readChangeDocumentTask = executor.submit(new ReadChangeDocument());
    }
    
    public ChangeDocumentPoolEntry(DocumentFactory factory, File historyFile, ChangeHistory changes) {
        this.factory = factory;
        this.historyFile = historyFile;
        this.changeDocument = changes;
        touch();
        executor.submit(new WriteChanges(changes));
    }
    
    public ChangeHistory getChangeDocument() throws OWLServerException {
        touch();
        if (changeDocument == null) {
            try {
                changeDocument = readChangeDocumentTask.get();
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
        return changeDocument;
    }
    
    public void setChangeDocument(final ChangeHistory newChangeDocument) {
    	if (logger.isLoggable(Level.FINE)) {
    		logger.fine("Setting change document for " + historyFile + " to change doc ending at revision " + newChangeDocument.getEndRevision());
    	}
        touch();
        this.changeDocument = newChangeDocument;
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
        public ChangeHistory call() throws IOException {
            File backup = getBackupHistoryFile(historyFile);
            try {
                return ChangeHistoryUtilities.readChanges(factory, historyFile, OntologyDocumentRevision.START_REVISION, null);
            }
            catch (RuntimeException err) {
                if (backup.exists()) {
                    return ChangeHistoryUtilities.readChanges(factory, backup, OntologyDocumentRevision.START_REVISION, null);
                }
                else {
                    throw err;
                }
            }
            catch (IOException ioe) {
                if (backup.exists()) {
                    return ChangeHistoryUtilities.readChanges(factory, backup, OntologyDocumentRevision.START_REVISION, null);
                }
                else {
                    throw ioe;
                }
            }
        }
    }
    
    private class WriteChanges implements Callable<Boolean> {
        private ChangeHistory newChangeDocument;
        
        public WriteChanges(ChangeHistory newChangeDocument) {
            this.newChangeDocument = newChangeDocument;
            if (logger.isLoggable(Level.FINE)) {
            	logger.fine("Created writer for " + historyFile + " and change document ending at " + newChangeDocument.getEndRevision());
            }
        }
        
        @Override
        public Boolean call() {
            try {
                if (changeDocument == newChangeDocument) {
                    prepareToSave(historyFile);
                    
                    long startTime = System.currentTimeMillis();
                                        
                    ChangeHistoryUtilities.writeChanges(newChangeDocument, historyFile);
                    
                    long interval = System.currentTimeMillis() - startTime;
                    if (interval > 1000) {
                        logger.info("Save of " + historyFile + " took " + (interval / 1000) + " seconds.");
                    }
                    else if (logger.isLoggable(Level.FINE)) {
                    	logger.fine("Wrote new " + historyFile);
                    }
                }
                else if (logger.isLoggable(Level.FINE)) {
                	logger.fine("This is not the latest change document");
                	logger.fine("Was supposed to save doc with end revision " + newChangeDocument.getEndRevision());
                	logger.fine("But now have new save doc with end revision " + changeDocument.getEndRevision());
                }
                return true;
            }
            catch (Throwable t) {
                logger.log(Level.SEVERE, "Exception caught writing history file", t);
                return false;
            }
        }
        
        private void prepareToSave(File historyFile) {
        	if (logger.isLoggable(Level.FINE)) {
        		logger.fine("Preparing backup for " + historyFile);
        	}
            File backup = getBackupHistoryFile(historyFile);
            if (historyFile.exists() && backup.exists()) {
                backup.delete();
                if (logger.isLoggable(Level.FINE)) {
                	logger.fine("Old backup removed");
                }
            }
            if (historyFile.exists()) {
                historyFile.renameTo(backup);
                if (logger.isLoggable(Level.FINE)) {
                	logger.fine("Moved " + historyFile + " to " + backup);
                }
            }
        }
    }
    
    private File getBackupHistoryFile(File historyFile) {
        String path = historyFile.getAbsolutePath();
        return new File(path + ".~");
    }
    
        
}
