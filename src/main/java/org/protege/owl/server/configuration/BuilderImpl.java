package org.protege.owl.server.configuration;

import java.util.logging.Logger;

import org.protege.owl.server.api.Builder;
import org.protege.owl.server.api.ServerComponentFactory;
import org.semanticweb.owlapi.model.OWLOntology;

public class BuilderImpl implements Builder {
	private Logger logger = Logger.getLogger(BuilderImpl.class.getCanonicalName());
	
	@Override
	public void setConfiguration(OWLOntology configuration) {

	}
	
	public void addServerComponentFactory(ServerComponentFactory factory) {
		logger.info("New server component factory: " + factory);
	}
	
	public void removeServerComponentFactory(ServerComponentFactory factory) {
		logger.info("Disabling server component factory: " + factory);
	}
}
