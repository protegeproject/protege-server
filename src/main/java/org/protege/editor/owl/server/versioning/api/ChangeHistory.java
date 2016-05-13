package org.protege.editor.owl.server.versioning.api;

import org.protege.editor.owl.server.versioning.RevisionMetadata;
import org.protege.editor.owl.server.versioning.DocumentRevision;

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

    void addRevision(RevisionMetadata metadata, List<OWLOntologyChange> changes);

    SortedMap<DocumentRevision, List<OWLOntologyChange>> getRevisions();

    SortedMap<DocumentRevision, RevisionMetadata> getMetadata();

    /**
     * Returns the ChangeMetaData (user, date of change) for the change document
     * at a given revision.
     * <p>
     * It should be non-null for all revisions from the start revision of the
     * change document (inclusive) to the end revision of the document
     * (exclusive). Passing any other revision id in leads to unknown results.
     * 
     * @param revision
     *            revision
     * @return ChangeMetaData
     */
    RevisionMetadata getMetadataForRevision(DocumentRevision revision);

    List<OWLOntologyChange> getChangesForRevision(DocumentRevision revision);

    boolean isEmpty();
}
