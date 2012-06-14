package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.OWLIndividual;

public interface ServerComponentFactory {
	
	Server createServer(OWLIndividual i);
	
	ServerFilter createServerFilter(OWLIndividual i);
	
	ServerTransport createServerTransport(OWLIndividual i);
}
