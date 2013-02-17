package org.protege.owl.server.configuration;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.SERVER;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.server.Builder;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerComponentFactory;
import org.protege.owl.server.api.server.ServerConfiguration;
import org.protege.owl.server.api.server.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class BuilderImpl implements Builder {
	private Logger logger = Logger.getLogger(BuilderImpl.class.getCanonicalName());
	private OWLOntology metaOntology;
	private Set<ServerComponentFactory> factories = new HashSet<ServerComponentFactory>();
	private ServerConstraints serverConstraints;
	
	private Server server;
	private List<ServerTransport> serverTransports;
	
	@Override
	public void initialise(ServerConfiguration configuration) {
		this.metaOntology = configuration.getMetaOntology();
		logger.info("Server configuration found");
		setupConstraints();	
	}
	
	@Override
	public void addServerComponentFactory(ServerComponentFactory factory) {
	    try {
	        factories.add(factory);
	        logger.info("New server component factory: " + factory);
	        factory.setConfiguration(metaOntology);
	        satisfyConstraints();
	    }
		catch (IOException ioe) {
			logger.log(Level.SEVERE, "Exception caught configuring server", ioe);
		}
	}
	
	@Override
	public void removeServerComponentFactory(ServerComponentFactory factory) {
		try {
			logger.info("Disabling server component factory: " + factory);
			factories.remove(factory);
			if (isUp() && metaOntology != null) {
				logger.info("Resetting server");
				server.shutdown();
				server = null;
				serverTransports.clear();
				satisfyConstraints();
				if (!isUp()) {
					logger.info("Server is down");
				}
			}
		}
		catch (IOException ioe) {
			logger.log(Level.SEVERE, "Exception caught while withdrawing server component factory", ioe);
		}
	}
	
	@Override
	public void deactivate() {
	    if (server != null) {
	        server.shutdown();
	    }
	}
	
	@Override
	public boolean isUp() {
		return server != null;
	}
	
	private void setupConstraints() {
		for (OWLIndividual individual : MetaprojectVocabulary.getIndividuals(metaOntology, SERVER)) {
			serverConstraints = new ServerConstraints(metaOntology, individual); // assume just one for now...
			break;
		}
	}
	
	private void satisfyConstraints() throws IOException {
		boolean success = false;
		try {
			if (!isUp() && serverConstraints.satisfied(factories)) {
				server = serverConstraints.buildServer(factories);
				serverTransports = serverConstraints.buildServerTransports(factories, server);
				server.setTransports(serverTransports);
				logger.info("Server started");
			}
			success = true;
		}
		finally {
			if (!success) {
				server = null;
				if (serverTransports != null) {
					serverTransports.clear();
				}
			}
		}
	}
}
