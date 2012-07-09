package org.protege.owl.server.configuration;

import java.util.logging.Logger;

import org.protege.owl.server.api.ServerComponentFactory;

public class BuilderImpl {
	private Logger logger = Logger.getLogger(BuilderImpl.class.getCanonicalName());
	
	
	public void addServerComponentFactory(ServerComponentFactory factory) {
		logger.info("New server component factory: " + factory);
	}
	
	public void removeServerComponentFactory(ServerComponentFactory factory) {
		logger.info("Disabling server component factory: " + factory);
	}
}
