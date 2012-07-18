package org.protege.owl.server.configuration;

import java.util.Set;

import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;

public class TransportConstraints {
	
	public TransportConstraints(OWLIndividual transportIndividual) {
		
	}

	
	public boolean satisfied(Set<ServerComponentFactory> factories) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public ServerTransport build(Set<ServerComponentFactory> factories) {
		// TODO Auto-generated method stub
		return null;
	}
	
}