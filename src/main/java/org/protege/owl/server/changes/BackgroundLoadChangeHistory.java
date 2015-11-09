package org.protege.owl.server.changes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * A change history that is loaded from a separate file but which loads the history in a separate thread so that
 * you will not have to wait for the thread to finish loading but only will need to wait when you make some query against the
 * change history.
 * <p>
 * A strange constraint on this class is that if the change history class is corrupted then the exception is not seen until the
 * ChangeHistory is used.  Instead of throwing an error at that point, this class will return the empty history.  This somewhat strange behavior
 * is actually perfectly acceptable to a caller such as an implementation of the VersionedOntologyDocument because the history document is a cache
 * and having it suddenly become empty merely means that it will need to be refilled later.
 */
public class BackgroundLoadChangeHistory implements ChangeHistory {
    private Logger logger = Logger.getLogger(BackgroundLoadChangeHistory.class.getCanonicalName());
    private File historyFile;
    private DocumentFactory factory;
    private FutureTask<ChangeHistory> backgroundLoader;
    private ChangeHistory delegateOnFailure;
    
    public BackgroundLoadChangeHistory(DocumentFactory factory, File historyFile) {
        this.factory = factory;
        this.historyFile = historyFile;
        backgroundLoader = new FutureTask<ChangeHistory>(new BackGroundLoader());
        new Thread(backgroundLoader, "History loading thread").start();
    }
    
    private class BackGroundLoader implements Callable<ChangeHistory> {

        @Override
        public ChangeHistory call() throws IOException {
            long startTime = System.currentTimeMillis();
            FileInputStream in = new FileInputStream(historyFile);
            try {
                return factory.readChangeDocument(in, null, null);
            }
            finally {
                in.close();
                long interval = System.currentTimeMillis() - startTime;
                if (interval >= 1000) {
                    logger.info("Load of history file " + historyFile.getName() + " took " + (interval / 1000) + " seconds.");
                }
            }
        }
        
    }

    private ChangeHistory getDelegate() {
        ChangeHistory history = delegateOnFailure;
        if (delegateOnFailure == null) {
            try {
                history = backgroundLoader.get();
            }
            catch (Exception e) {
                logger.log(Level.WARNING, "History File " + historyFile + " was corrupted - using empty history instead.", e);
                delegateOnFailure = factory.createEmptyChangeDocument(OntologyDocumentRevision.START_REVISION);
                history = delegateOnFailure;
            }
        }
        return history;
    }

    public DocumentFactory getDocumentFactory() {
        return factory;
    }


    public OntologyDocumentRevision getStartRevision() {
        return getDelegate().getStartRevision();
    }


    public OntologyDocumentRevision getEndRevision() {
        return getDelegate().getEndRevision();
    }


    public ChangeMetaData getMetaData(OntologyDocumentRevision revision) {
        return getDelegate().getMetaData(revision);
    }


    public ChangeHistory cropChanges(OntologyDocumentRevision start, OntologyDocumentRevision end) {
        return getDelegate().cropChanges(start, end);
    }


    public ChangeHistory appendChanges(ChangeHistory additionalChanges) {
        return getDelegate().appendChanges(additionalChanges);
    }


    public List<OWLOntologyChange> getChanges(OWLOntology ontology) {
        return getDelegate().getChanges(ontology);
    }


    public void writeChangeDocument(OutputStream out) throws IOException {
        getDelegate().writeChangeDocument(out);
    }


    public void setCompressionLimit(int compressionLimit) {
        getDelegate().setCompressionLimit(compressionLimit);
    }


}
