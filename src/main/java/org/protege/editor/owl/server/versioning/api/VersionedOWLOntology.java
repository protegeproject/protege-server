package org.protege.editor.owl.server.versioning.api;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.io.IOException;
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
     * Returns the change recording that contains all the revisions that are created during
     * the track changes. The returned object is associated to the <b>local</b> change history.
     */
    ChangeHistory getChangeHistory();

    /**
     * Records a revision specified by the given <code>metadata</code> and <code>changes</code>.
     *
     * @param metadata
     *          The meta information about the change (e.g., author details, comment)
     * @param changes
     *          A list of changes that make this revision.
     */
    void addRevision(RevisionMetadata metadata, List<OWLOntologyChange> changes);

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

    /**
     * This call will save the data about the ontology connection, including the
     * server document IRI, the local revision and the local history.
     * <p>
     * The success of this call depends on where the ontology document resides.
     * If the ontology is stored in a file in a file system then an
     * implementation of this call could save the ontology connection data in
     * some files in some directory near the ontology file. However if the
     * ontology is stored somewhere on the web, it is not clear that there will
     * be a way to write this metadata. But one could imagine that some future
     * implementation of this method would use web-dav or something similar to
     * save this data.
     * <p>
     * This routine will also save the history data as is done by the
     * saveLocalHistory() function call.
     * 
     * 
     * @return true only if it successfully saved the connection data. It will
     *         return false if the location where the ontology is stored is not
     *         suitable for saving meta-data associated with the ontology.
     * @throws IOException
     *             IOException
     */
    boolean saveMetadata() throws Exception;

    /**
     * This call will save the local history associated with a versioned
     * ontology document.
     * <p>
     * The success of this call depends on where the ontology document resides
     * exactly as described in the documentation of the saveMetaData() call. The
     * reason that this call isn't simply subsumed in the saveMetaData call is
     * that there are occasions where the local history can be saved more
     * aggressively than the meta data can. For example, an ontology editor
     * should probably not save the metadata associated with an ontology until
     * the ontology is saved. That is the ontology editor is responsible for
     * keeping the metadata synchronized with the copy of the ontology on disk.
     * However the change history can be saved anytime it is updated because the
     * change history for an ontology document can not change structurally, it
     * can only be appended to.
     * 
     * @return boolean
     * @throws IOException
     *             IOException
     */
    boolean saveLocalHistory() throws Exception;
}
