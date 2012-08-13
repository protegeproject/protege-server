package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.HAS_HOST_PORT;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.RMI_TRANSPORT;

import java.util.Set;
import java.util.logging.Logger;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.connect.rmi.RMITransport;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

public class RMIConnectionFactory implements ServerComponentFactory {
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
        return false;
    }

    @Override
    public Server createServer(OWLIndividual i) {
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
        return "RMI Transport Factory";
    }

}
