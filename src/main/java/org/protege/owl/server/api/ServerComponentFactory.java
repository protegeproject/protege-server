package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.OWLIndividual;

public interface ServerComponentFactory {
	
	boolean hasSuitableServer(OWLIndividual i);
	
	Server createServer(OWLIndividual i);
	
	boolean hasSuitableServerFilter(OWLIndividual i);
	
	ServerFilter createServerFilter(OWLIndividual i, Server server);
	
	ServerTransport hasSuitableServerTransport(OWLIndividual i);
	
	ServerTransport createServerTransport(OWLIndividual i);
}
