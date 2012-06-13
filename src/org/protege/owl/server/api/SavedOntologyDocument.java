package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.IRI;

public interface SavedOntologyDocument {

	IRI getLocalAddress();
	
	ChangeDocument getLocalHistory();
	
	void addToLocalHistory(ChangeDocument changes);
	
	RemoteOntologyDocument getServerDocument();
}
