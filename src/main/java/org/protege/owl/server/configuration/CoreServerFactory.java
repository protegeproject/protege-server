package org.protege.owl.server.configuration;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_HOST_PORT;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_ROOT_PATH;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.RMI_TRANSPORT;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.STANDARD_SERVER;

import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.connect.rmi.RMITransport;
import org.protege.owl.server.impl.ServerImpl;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

public class CoreServerFactory implements ServerComponentFactory {
	public static Logger logger = Logger.getLogger(CoreServerFactory.class.getCanonicalName());
	private OWLOntology ontology;
	private OWLDataFactory factory;
	
	@Override
	public void setConfiguration(OWLOntology ontology) {
		this.ontology = ontology;
		factory = ontology.getOWLOntologyManager().getOWLDataFactory();
	}

	@Override
	public boolean hasSuitableServer(OWLIndividual i) {
		OWLAxiom rightType = factory.getOWLClassAssertionAxiom(STANDARD_SERVER, i);
		boolean hasRightType = ontology.containsAxiom(rightType);
		Set<OWLLiteral> roots = i.getDataPropertyValues(HAS_ROOT_PATH, ontology);
		if (hasRightType && (roots == null || roots.size() != 1)) {
			logger.warning("OWL Individual, " + i + " has the right type for a standard server plugin but doesn't have its root path configured correctly");
			return false;
		}
		return hasRightType;
	}

	@Override
	public Server createServer(OWLIndividual i) {
		for (OWLLiteral rootPathLiteral : i.getDataPropertyValues(HAS_ROOT_PATH, ontology)) {
			String rootPath = rootPathLiteral.getLiteral();
			return new ServerImpl(new File(rootPath));
		}
		return null;
	}

	@Override
	public boolean hasSuitableServerFilter(OWLIndividual i) {
		return false;
	}

	@Override
	public ServerFilter createServerFilter(OWLIndividual i, Server server) {
		return null;
	}

	@Override
	public boolean hasSuitableServerTransport(OWLIndividual i) {
		OWLAxiom rightType = factory.getOWLClassAssertionAxiom(RMI_TRANSPORT, i);
		boolean hasRightType = ontology.containsAxiom(rightType);
		Set<OWLLiteral> ports = i.getDataPropertyValues(HAS_HOST_PORT, ontology);
		if (hasRightType && (ports == null || ports.size() != 1)) {
			logger.warning("OWL Individual " + i + " has the right type for an rmi transport plugin but doesn't have its ports correctly configured");
			return false;
		}
		return hasRightType;
	}

	@Override
	public ServerTransport createServerTransport(OWLIndividual i) {
		for (OWLLiteral hostPortLiteral : i.getDataPropertyValues(HAS_HOST_PORT, ontology)) {
			if (hostPortLiteral.isInteger()) {
				int port = hostPortLiteral.parseInteger();
				return new RMITransport(port);
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "<<Core Server Components>>";
	}

}
