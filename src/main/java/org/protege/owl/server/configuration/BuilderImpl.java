package org.protege.owl.server.configuration;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.SERVER;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.Builder;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class BuilderImpl implements Builder {
	private Logger logger = Logger.getLogger(BuilderImpl.class.getCanonicalName());
	private OWLOntology configuration;
	private Set<ServerComponentFactory> factories = new HashSet<ServerComponentFactory>();
	private ServerConstraints serverConstraints;
	
	private Server server;
	private List<ServerTransport> serverTransports;
	
	@Override
	public void setConfiguration(OWLOntology configuration) {
		this.configuration = configuration;
		logger.info("Server configuration found");
		for (ServerComponentFactory factory : factories) {
			logger.info("New server component factory: " + factory);
			factory.setConfiguration(configuration);
		}
		setupConstraints();
		try {
			satisfyConstraints();
		}
		catch (IOException ioe) {
			logger.log(Level.SEVERE, "Exception caught configuring server", ioe);
		}		
	}
	
	@Override
	public void addServerComponentFactory(ServerComponentFactory factory) {
		try {
			factories.add(factory);
			if (configuration != null) { // stay silent if we don't know if the server is going to run...
				logger.info("New server component factory: " + factory);
				factory.setConfiguration(configuration);
				satisfyConstraints();
			}
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
			if (isUp() && configuration != null) {
				logger.info("Resetting server");
				server.shutdown();
				for (ServerTransport serverTransport : serverTransports) {
					serverTransport.dispose();
				}
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
	public boolean isUp() {
		return server != null;
	}
	
	private void setupConstraints() {
		for (OWLIndividual individual : MetaprojectVocabulary.getIndividuals(configuration, SERVER)) {
			serverConstraints = new ServerConstraints(configuration, individual); // assume just one for now...
			break;
		}
	}
	
	private void satisfyConstraints() throws IOException {
		boolean success = false;
		try {
			if (!isUp() && serverConstraints.satisfied(factories)) {
				server = serverConstraints.buildServer(factories);
				serverTransports = serverConstraints.buildServerTransports(factories, server);
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
