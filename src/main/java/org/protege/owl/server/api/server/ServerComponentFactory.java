package org.protege.owl.server.api.server;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public interface ServerComponentFactory {
	
	void setConfiguration(OWLOntology ontology);
	
	boolean hasSuitableServer(OWLIndividual i);
	
	Server createServer(OWLIndividual i);
	
	boolean hasSuitableServerFilter(OWLIndividual i);
	
	ServerFilter createServerFilter(OWLIndividual i, Server server);
	
	boolean hasSuitableServerTransport(OWLIndividual i);
	
	ServerTransport createServerTransport(OWLIndividual i);
}
