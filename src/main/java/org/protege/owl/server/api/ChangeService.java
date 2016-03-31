package org.protege.owl.server.api;

import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.api.ChangeHistory;

import edu.stanford.protege.metaproject.api.Address;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public interface ChangeService {

    /**
     * Compute the changes given the input resource location and the revision range.
     *
     * @param resourceLocation
     *            The input resource location
     * @param startRevision
     *            The start revision.
     * @param endRevision
     *            The end revision.
     * @return The change history from the start revision until the end revision
     * @throws Exception
     */
    ChangeHistory getChanges(Address resourceLocation, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception;
}
