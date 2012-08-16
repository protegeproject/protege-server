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

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.exception.ServerException;
import org.protege.owl.server.changes.ChangeDocumentUtilities;

public class ChangeDocumentPoolEntry {
    private Logger logger = Logger.getLogger(ChangeDocumentPoolEntry.class.getCanonicalName());
    private DocumentFactory factory;
    private ChangeDocument changeDocument;
    private Future<ChangeDocument> readChangeDocumentTask;
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
    
    public ChangeDocumentPoolEntry(DocumentFactory factory, File historyFile, ChangeDocument changes) {
        this.factory = factory;
        this.historyFile = historyFile;
        this.changeDocument = changes;
        touch();
        executor.submit(new WriteChanges(changes));
    }
    
    public ChangeDocument getChangeDocument() throws ServerException {
        touch();
        if (changeDocument == null) {
            try {
                changeDocument = readChangeDocumentTask.get();
            }
            catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
            catch (ExecutionException ee) {
                if (ee.getCause() instanceof ServerException) {
                    throw (ServerException) ee.getCause();
                }
                else {
                    throw new RuntimeException(ee);
                }
            }
        }
        return changeDocument;
    }
    
    public void setChangeDocument(final ChangeDocument newChangeDocument) {
        touch();
        executor.submit(new WriteChanges(newChangeDocument));
        this.changeDocument = newChangeDocument;
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
    
    private class ReadChangeDocument implements Callable<ChangeDocument> {
        @Override
        public ChangeDocument call() throws IOException {
            return ChangeDocumentUtilities.readChanges(factory, historyFile, OntologyDocumentRevision.START_REVISION, null);
        }
    }
    
    private class WriteChanges implements Callable<Boolean> {
        private ChangeDocument newChangeDocument;
        
        public WriteChanges(ChangeDocument newChangeDocument) {
            this.newChangeDocument = newChangeDocument;
        }
        
        @Override
        public Boolean call() {
            try {
                if (changeDocument == newChangeDocument) {
                    long startTime = System.currentTimeMillis();
                    
                    ChangeDocumentUtilities.writeChanges(newChangeDocument, historyFile);
                    
                    long interval = System.currentTimeMillis() - startTime;
                    if (interval > 1000) {
                        logger.info("Save of " + historyFile + " took " + (interval / 1000) + " seconds.");
                    }
                }
                return true;
            }
            catch (Throwable t) {
                logger.log(Level.SEVERE, "Exception caught writing history file", t);
                return false;
            }
        }
    }
    
        
}
