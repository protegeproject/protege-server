package org.protege.owl.server.impl;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.RemoteOntologyDocument;

public class ChangeDocumentPool {
    private DocumentFactory docFactory;
    private Map<RemoteOntologyDocument, ChangeDocumentPoolEntry> pool = new TreeMap<RemoteOntologyDocument, ChangeDocumentPoolEntry>();
    
    public ChangeDocumentPool(DocumentFactory docFactory) {
        this.docFactory = docFactory;
    }
    
    public ChangeDocument getChangeDocument(RemoteOntologyDocument doc, File historyFile) {
        ChangeDocumentPoolEntry entry;
        synchronized (pool) {
            entry = pool.get(doc);
            if (entry == null) {
                entry = new ChangeDocumentPoolEntry(historyFile);
                pool.put(doc, entry);
            }
        }
        return entry.getChangeDocument();
    }
    
    public void setChangeDocument(RemoteOntologyDocument doc, File historyFile, ChangeDocument changes) {
        synchronized (pool) {
            ChangeDocumentPoolEntry entry = pool.get(doc);
            if (pool != null) {
                entry.setChangeDocument(changes);
            }
            else {
                entry = new ChangeDocumentPoolEntry(historyFile, changes);
                pool.put(doc, entry);
            }
        }
    }
    
    
    private class ChangeDocumentPoolEntry {
        private ChangeDocument changeDocument;
        private FutureTask<ChangeDocument> readChangeDocumentTask;
        private File historyFile;
        private Date touched;
        private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
           @Override
            public Thread newThread(Runnable r) {
               Thread thread = new Thread(r, "Change Document Updates for " + historyFile);
               thread.setDaemon(true);
               return thread;
            } 
        });
        
        public ChangeDocumentPoolEntry(File historyFile) {
            this.historyFile = historyFile;
            this.touched = new Date();
            readChangeDocumentTask = executor.submit(new Callable<ChangeDocument>() {
                @Override
                public ChangeDocument call() throws Exception {
                    return ChangeDocumentUtilities.
                }
            });
        }
        
        public ChangeDocumentPoolEntry(File historyFile, ChangeDocument changes) {
            this.historyFile = historyFile;
            this.touched = touched;
            this.changeDocument = changes;
        }
        
        public ChangeDocument getChangeDocument() {
            this.touched = new Date();
            return changeDocument;
        }
        
        public void setChangeDocument(ChangeDocument changeDocument) {
            this.touched = new Date();
            this.changeDocument = changeDocument;
        }
        
    };
}
