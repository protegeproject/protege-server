package org.protege.owl.server.configuration;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.protege.owl.server.api.server.ServerComponentFactory;
import org.protege.owl.server.api.server.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class TransportConstraints {
    private Logger logger = LoggerFactory.getLogger(TransportConstraints.class.getCanonicalName());
	private OWLIndividual transportIndividual;
	
	public TransportConstraints(OWLOntology configuration, OWLIndividual transportIndividual) {
		this.transportIndividual = transportIndividual;
	}

	
	public boolean satisfied(Set<ServerComponentFactory> factories) {
		for (ServerComponentFactory factory : factories) {
			if (factory.hasSuitableServerTransport(transportIndividual)) {
			    if (logger.isDebugEnabled()) {
			        logger.debug("Using " + factory + " to satisfy constraint: " + transportIndividual);
			    }
				return true;
			}
		}
		if (logger.isDebugEnabled()) {
		    logger.debug("Could not find factory to satisfy constraint: " + transportIndividual);
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