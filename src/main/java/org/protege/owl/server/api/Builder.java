package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.OWLOntology;

public interface Builder {
	void setConfiguration(OWLOntology configuration);
	
	void addServerComponentFactory(ServerComponentFactory factory);

	void removeServerComponentFactory(ServerComponentFactory factory);
	
	boolean isUp();
}
