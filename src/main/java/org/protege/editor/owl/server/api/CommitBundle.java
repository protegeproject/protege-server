package org.protege.editor.owl.server.api;

import org.protege.editor.owl.server.versioning.Commit;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;

import java.io.Serializable;
import java.util.List;

/**
 * Represents the whole commit changes that users send to the server.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface CommitBundle extends Serializable {

    /**
     * @deprecated Use getBaseRevision() instead.
     */
    @Deprecated
    DocumentRevision getHeadRevision();

    DocumentRevision getBaseRevision();

    List<Commit> getCommits();
}
