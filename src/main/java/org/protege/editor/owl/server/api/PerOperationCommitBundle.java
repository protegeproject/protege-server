package org.protege.editor.owl.server.api;

import edu.stanford.protege.metaproject.api.Operation;

/**
 * Represents a pack of commit changes where they are coming from a single
 * app-defined operation.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface PerOperationCommitBundle extends CommitBundle {

    Operation getOperation();
}
