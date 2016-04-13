package org.protege.owl.server.api;

import org.protege.owl.server.changes.HistoryFile;
import org.protege.owl.server.changes.OntologyDocumentRevision;
import org.protege.owl.server.changes.api.ChangeHistory;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 *         Stanford Center for Biomedical Informatics Research
 */
public interface ChangeService {

    /**
     * Compute the changes given the input resource location and the revision range.
     *
     * @param historyFile
     *            The source history record
     * @param startRevision
     *            The start revision.
     * @param endRevision
     *            The end revision.
     * @return The change history from the start revision until the end revision
     * @throws Exception
     */
    ChangeHistory getChanges(HistoryFile historyFile,
            OntologyDocumentRevision startRevision,
            OntologyDocumentRevision endRevision) throws Exception;

    ChangeHistory getLatestChanges(HistoryFile historyFile,
            OntologyDocumentRevision startRevision) throws Exception;

    ChangeHistory getAllChanges(HistoryFile historyFile) throws Exception;

    OntologyDocumentRevision getHeadRevision(HistoryFile historyFile) throws Exception;
}
