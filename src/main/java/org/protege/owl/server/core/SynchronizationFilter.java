package org.protege.owl.server.core;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.SingletonChangeHistory;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerOntologyDocument;
import org.protege.owl.server.util.ServerFilterAdapter;

/**
 * A mandatory filter that is placed outside of all the other filters and provides
 * synchronization for thread safety.
 * <p>
 * Currently this class only protects the commit operation.  There may be a case in the
 * future to extend this but for now here is the case why the commit operation and only the
 * commit operation needs synchronization.  It will help in understanding this argument to know 
 * that the change history for a server ontology document is a thread safe immutable structure
 * which is retrieved and set when needed from a thread safe change history pool.
 * <p>
 * First imagine that two commit operations on the same server ontology document are occurring concurrently. 
 * If the first commit completes before the second commit has finished doing either its conflict management 
 * processing or its calculation of the changes to commit, then these calculations may retrieve the wrong copy
 * of the change history for its calculations.  In particular it is possible to imagine a scenario where
 * both commit operations have calculated their new change history and are ready to swap the new change history in,
 * then the operation that overwrites the change history second will replace the changes from the first commit and
 * the changes from the first commit operation will be lost.
 * <p>
 * I don't think that the getChanges call requires synchronization because it requires only a single get operation 
 * on the change history pool.  Our current filters do not need to do any get operations.  Their influence on the getChanges 
 * operation currently only involves things like ensuring that the policy allows a read operation and that the calling user is 
 * authenticated.
 * <p>
 * Finally there are several calls that involve the file system like the create ontology document or the create directory calls.
 * For these, I am not sure what the synchronization issues are but I think that this is an edge case.
 * <p>
 * This may enforce a more stringent synchronization policy in the future.
 * 
 * @author tredmond
 *
 */
public class SynchronizationFilter extends ServerFilterAdapter {
    private Logger logger = Logger.getLogger(SynchronizationFilter.class.getCanonicalName());
    private Set<ServerOntologyDocument> writers = new TreeSet<ServerOntologyDocument>();
    
    public SynchronizationFilter(Server delegate) {
        super(delegate);
    }
    
    @Override
    public void commit(AuthToken u, ServerOntologyDocument doc, SingletonChangeHistory changes) throws OWLServerException {
        synchronized (writers) {
            while (writers.contains(doc)) {
                try {
                    writers.wait();
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Unexpected interrupt.", e);
                    writers.remove(doc);
                    throw new IllegalStateException("Unexpected Interrupt - server changes made at this time might be lost", e);
                }
            }
            writers.add(doc);
        }
        try {
            super.commit(u, doc, changes);
        }
        finally {
            synchronized (writers) {
                writers.remove(doc);
                writers.notifyAll();
            }
        }
    }

}
