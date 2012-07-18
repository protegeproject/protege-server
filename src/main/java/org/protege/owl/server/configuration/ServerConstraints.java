package org.protege.owl.server.configuration;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_TRANSPORT;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class ServerConstraints {
	private OWLOntology configuration;
	private OWLIndividual serverIndividual;

	private List<TransportConstraints> transportConstraints = new ArrayList<TransportConstraints>();

	private ServerComponentFactory factory;
	
	public ServerConstraints(OWLOntology configuration, OWLIndividual serverIndividual) {
		this.configuration = configuration;
		this.serverIndividual = serverIndividual;
		Set<OWLIndividual> transports = serverIndividual.getObjectPropertyValues(HAS_TRANSPORT, configuration);
		if (transports != null) {
			for (OWLIndividual transport : transports) {
				transportConstraints.add(new TransportConstraints(transport));
			}
		}
	}


	public boolean satisfied(Set<ServerComponentFactory> factories) {
		for (TransportConstraints constraint : transportConstraints) {
			if (!constraint.satisfied(factories)) {
				return false;
			}
		}
		
		if (factory == null) {
			for (ServerComponentFactory factory : factories) {
				if (factory.hasSuitableServer(serverIndividual)) {
					return true;
				}
			}
		}
		return false;
	}


	public List<ServerTransport> buildServerTransports(Set<ServerComponentFactory> factories, Server server) {
		List<ServerTransport> transports = new ArrayList<ServerTransport>();
		for (TransportConstraints constraint : transportConstraints) {
			ServerTransport transport = constraint.build(factories);
			transports.add(transport);
			transport.start(server);
		}
		return transports;
	}
	
	public Server buildServer() {
		return factory.createServer(serverIndividual);
	}
	

}