package org.protege.owl.server.api;

import java.io.IOException;

import org.protege.owl.server.impl.RemoteOntologyDocumentImpl;

public interface CommitWhiteBoard {
	void init(RemoteOntologyDocument serverDocument, String commitComment, ChangeDocument proposedChanges);

	ChangeDocument getFullChanges() throws IOException;
	
	ChangeDocument getServerChangesSinceCommit() throws IOException;
}
