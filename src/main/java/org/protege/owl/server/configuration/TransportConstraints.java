package org.protege.owl.server.configuration;

import java.util.Set;

import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class TransportConstraints {
	private OWLIndividual transportIndividual;
	
	public TransportConstraints(OWLOntology configuration, OWLIndividual transportIndividual) {
		this.transportIndividual = transportIndividual;
	}

	
	public boolean satisfied(Set<ServerComponentFactory> factories) {
		for (ServerComponentFactory factory : factories) {
			if (factory.hasSuitableServerTransport(transportIndividual)) {
				return true;
			}
		}
		return false;
	}

	
	public ServerTransport build(Set<ServerComponentFactory> factories) {
		for (ServerComponentFactory factory : factories) {
			if (factory.hasSuitableServerTransport(transportIndividual)) {
				return factory.createServerTransport(transportIndividual);
			}
		}
		throw new IllegalStateException("Expected to be ready to build a transport");
	}
	
}