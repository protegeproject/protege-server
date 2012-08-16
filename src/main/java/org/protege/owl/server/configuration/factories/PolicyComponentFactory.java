package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.BASIC_AUTHENTICATION_MANAGER;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.policy.Authenticator;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class PolicyComponentFactory implements ServerComponentFactory {
    private OWLOntology metaOntology;
    private OWLDataFactory factory;
    
    @Override
    public void setConfiguration(OWLOntology ontology) {
        metaOntology = ontology;
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
        OWLAxiom hasRightType = factory.getOWLClassAssertionAxiom(BASIC_AUTHENTICATION_MANAGER, i);
        return metaOntology.containsAxiom(hasRightType);
    }

    @Override
    public ServerFilter createServerFilter(OWLIndividual i, Server server) {
        try {
            return new Authenticator(server);
        }
        catch (Exception e) {
            throw new RuntimeException("Factory failed to setup server", e);
        }
    }

    @Override
    public boolean hasSuitableServerTransport(OWLIndividual i) {
        return false;
    }

    @Override
    public ServerTransport createServerTransport(OWLIndividual i) {
        return null;
    }

}
