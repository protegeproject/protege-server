package org.protege.editor.owl.server.versioning.api;

import org.semanticweb.owlapi.model.OWLOntology;

import java.io.Serializable;
import java.util.List;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * @author Timothy Redmond (tredmond) <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface VersionedOWLOntology extends Serializable {

    /**
     * Returns a server document that stores the reference to a remote resource
     * which link to the local resource.
     */
    ServerDocument getServerDocument();

    /**
     * Returns the OWL ontology that is being tracked for changes.
     */
    OWLOntology getOntology();

    /**
     * Returns the change recording that contains all the revisions that are created and accepted
     * by the central repository server. The returned object is the <b>local</b> copy of the
     * remote change history.
     */
    ChangeHistory getChangeHistory();

    /**
     * Updates the ontology with the specified input <code>changeHistory</code>. The input instance
     * should represent changes that are already committed to the server and got accepted.
     *
     * @param changeHistory
     *          The changes that were accepted by the server.
     */
    void update(ChangeHistory changeHistory);

    /**
     * Retrieves a particular metadata specified by the given <code>revision</code>.
     *
     * @param revision
     *          The revision number (as {link DocumentRevision} instance) to get the metadata.
     * @return The revision metadata
     */
    RevisionMetadata getRevisionMetadata(DocumentRevision revision);

    /**
     * Retrieves the latest metadata items from the HEAD revision to the length of the given
     * <code>offset</code> size. Calling <code>getLatestRevisionMetadata(1)</code> equals to
     * getting the metadata of the latest revision.
     *
     * @param offset
     *          Length of latest revision to retrieve
     * @return A list of revision metadata
     */
    List<RevisionMetadata> getLatestRevisionMetadata(int offset);

    /**
     * Returns the local base revision number (local/BASE).
     */
    DocumentRevision getBaseRevision();

    /**
     * Returns the local head (latest) revision number (local/HEAD).
     */
    DocumentRevision getHeadRevision();
}
