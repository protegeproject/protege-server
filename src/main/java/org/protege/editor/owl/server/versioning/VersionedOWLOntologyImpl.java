package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;
import org.protege.editor.owl.server.versioning.api.ServerDocument;
import org.protege.editor.owl.server.versioning.api.VersionedOWLOntology;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.EvictingQueue;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * @author Timothy Redmond (tredmond) <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class VersionedOWLOntologyImpl implements VersionedOWLOntology {

    private static final long serialVersionUID = 807389509276703528L;

    private static final int REVISION_LOG_CACHE_SIZE = 10;

    /*
     * Log cache that stores the last REVISION_LOG_CACHE_SIZE revisions.
     */
    private EvictingQueue<RevisionMetadata> logCache = EvictingQueue.create(REVISION_LOG_CACHE_SIZE);

    private ServerDocument serverDocument;
    private OWLOntology ontology;
    private ChangeHistory changeHistory;

    /**
     * Creates a versioned ontology that tracks changes of the specified underlying OWL ontology.
     * The input <code>serverDocument</code> will contain the reference to a remote resource
     * for collaboration and public sharing.
     *
     * @param serverDocument
     *          A reference information to a remote resource.
     * @param ontology
     *          The ontology to be tracked for changes.
     */
    public VersionedOWLOntologyImpl(ServerDocument serverDocument, OWLOntology ontology) {
        this(serverDocument, ontology, ChangeHistoryImpl.createEmptyChangeHistory());
    }

    /**
     * Creates a versioned ontology that tracks changes of the specified underlying OWL ontology
     * with an addition of an initial change records specified by <code>changeHistory</code> input.
     * The input <code>serverDocument</code> will contain the reference to a remote resource
     * for collaboration and public sharing.
     *
     * @param serverDocument
     *          A reference information to a remote resource.
     * @param ontology
     *          The ontology to be tracked for changes.
     * @param changeHistory
     *          The initial change records of the input ontology.
     */
    public VersionedOWLOntologyImpl(ServerDocument serverDocument, OWLOntology ontology, ChangeHistory changeHistory) {
        this.serverDocument = serverDocument;
        this.ontology = ontology;
        this.changeHistory = changeHistory;
    }

    @Override
    public ServerDocument getServerDocument() {
        return serverDocument;
    }

    @Override
    public OWLOntology getOntology() {
        return ontology;
    }

    @Override
    public ChangeHistory getChangeHistory() {
        return changeHistory;
    }

    @Override
    public void update(ChangeHistory incomingChangeHistory) {
        final DocumentRevision base = incomingChangeHistory.getBaseRevision();
        final DocumentRevision end = incomingChangeHistory.getHeadRevision();
        for (DocumentRevision current = base; current.behind(end);) {
            current = current.next();
            RevisionMetadata metadata = incomingChangeHistory.getMetadataForRevision(current);
            List<OWLOntologyChange> changes = incomingChangeHistory.getChangesForRevision(current);
            changeHistory.addRevision(metadata, changes);
            logCache.add(metadata);
        }
    }

    @Override
    public RevisionMetadata getRevisionMetadata(DocumentRevision revision) {
        return changeHistory.getMetadataForRevision(revision);
    }

    @Override
    public List<RevisionMetadata> getLatestRevisionMetadata(int offset) {
        if (offset > REVISION_LOG_CACHE_SIZE) {
            offset = REVISION_LOG_CACHE_SIZE; // handle until the maximum cache size
        }
        return logCache.stream().limit(offset).collect(Collectors.toList());
    }

    @Override
    public DocumentRevision getBaseRevision() {
        return changeHistory.getBaseRevision();
    }

    @Override
    public DocumentRevision getHeadRevision() {
        return changeHistory.getHeadRevision();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getOntology().getOntologyID());
        sb.append("\n");
        sb.append("HEAD: ");
        sb.append(getHeadRevision());
        sb.append("\n\n");
        for (RevisionMetadata metadata : getLatestRevisionMetadata(5)) {
            sb.append(metadata.getLogMessage());
            sb.append("\n");
        }
        return sb.toString();
    }
}
