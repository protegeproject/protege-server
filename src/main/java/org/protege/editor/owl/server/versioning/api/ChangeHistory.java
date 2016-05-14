package org.protege.editor.owl.server.versioning.api;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.List;
import java.util.SortedMap;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * @author Timothy Redmond (tredmond) <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface ChangeHistory {

    String CHANGE_DOCUMENT_EXTENSION = ".history";

    /**
     * Get the base revision of this collection of changes.
     * 
     * @return OntologyDocumentRevision
     */
    DocumentRevision getBaseRevision();

    /**
     * Get the head revision of this collection of changes.
     * 
     * @return OntologyDocumentRevision
     */
    DocumentRevision getHeadRevision();

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
     * Get all the revisions stored in this change history. Each revision has a revision number
     * presented as {@link DocumentRevision}. This method returns a sorted revisions.
     *
     * @return All the revisions sorted by its revision number.
     */
    SortedMap<DocumentRevision, List<OWLOntologyChange>> getRevisions();

    /**
     * Get all the metadata associated to the revisions. This method returns a sorted metadata.
     *
     * @return All the revision metadata sorted by its revision number.
     */
    SortedMap<DocumentRevision, RevisionMetadata> getMetadata();

    /**
     * Retrieves a particular metadata specified by the given <code>revision</code>.
     *
     * @param revision
     *          The revision number (as {link DocumentRevision} instance) to get the metadata.
     * @return The revision metadata
     */
    RevisionMetadata getMetadataForRevision(DocumentRevision revision);

    /**
     * Retrieves a list of ontology changes specified by the given <code>revision</code>.
     *
     * @param revision
     *          The revision number (as {link DocumentRevision} instance) to get the changes.
     * @return A list of ontology changes.
     */
    List<OWLOntologyChange> getChangesForRevision(DocumentRevision revision);

    /**
     * Checks if the change history is empty or not.
     *
     * @return Returns <code>true</code> if it is empty, or <code>false</code> otherwise.
     */
    boolean isEmpty();
}
