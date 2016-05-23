package org.protege.editor.owl.server.api;

import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;

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
    ChangeHistory getChanges(HistoryFile historyFile, DocumentRevision startRevision, DocumentRevision endRevision)
            throws ServerServiceException;

    DocumentRevision getHeadRevision(HistoryFile historyFile) throws ServerServiceException;
}
