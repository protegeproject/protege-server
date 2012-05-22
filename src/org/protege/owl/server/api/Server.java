package org.protege.owl.server.api;

import java.util.Collection;
import java.util.List;

public interface Server {
	
	Collection<ServerPath> list(User u, ServerDirectory dir);
	
	ChangeDocument get(User u, OntologyDocument doc, ServerRevision rev);
	
	ChangeDocument getChanges(User u, OntologyDocument doc, ServerRevision start, ServerRevision end);
	
	OntologyDocument create(User u, String commitComment, OntologyDocument doc);
	
	void applyChange(User u, String commitComment, List<OntologyDocumentChange> changes);
}
