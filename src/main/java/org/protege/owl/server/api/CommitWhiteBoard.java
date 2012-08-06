package org.protege.owl.server.api;

import java.io.IOException;

public interface CommitWhiteBoard {
	void init(RemoteOntologyDocument serverDocument, ChangeMetaData commitComment, ChangeDocument proposedChanges);

	ChangeDocument getFullChanges() throws IOException;
	
	ChangeDocument getServerChangesSinceCommit() throws IOException;
}
