package org.protege.owl.server.api;

import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.api.ChangeHistory;

import java.io.File;

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
    ChangeHistory getChanges(File resourceLocation, OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception;

    ChangeHistory getAllChanges(File resourceLocation) throws Exception;

    ChangeHistory getLatestChanges(File resourceLocation, OntologyDocumentRevision startRevision)
            throws Exception;
}
