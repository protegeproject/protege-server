package org.protege.owl.server.configuration;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class TransportConstraints {
    private Logger logger = Logger.getLogger(TransportConstraints.class.getCanonicalName());
	private OWLIndividual transportIndividual;
	
	public TransportConstraints(OWLOntology configuration, OWLIndividual transportIndividual) {
		this.transportIndividual = transportIndividual;
	}

	
	public boolean satisfied(Set<ServerComponentFactory> factories) {
		for (ServerComponentFactory factory : factories) {
			if (factory.hasSuitableServerTransport(transportIndividual)) {
			    if (logger.isLoggable(Level.FINE)) {
			        logger.fine("Using " + factory + " to satisfy constraint: " + transportIndividual);
			    }
				return true;
			}
		}
		if (logger.isLoggable(Level.FINE)) {
		    logger.fine("Could not find factory to satisfy constraint: " + transportIndividual);
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