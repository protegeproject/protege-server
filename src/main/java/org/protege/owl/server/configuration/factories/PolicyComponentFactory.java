package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.BASIC_AUTHENTICATION_MANAGER;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.policy.Authenticator;
import org.protege.owl.server.util.ServerComponentFactoryAdapter;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class PolicyComponentFactory extends ServerComponentFactoryAdapter {
    private OWLOntology metaOntology;
    private OWLDataFactory factory;
    
    @Override
    public void setConfiguration(OWLOntology ontology) {
        metaOntology = ontology;
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
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
    
    public String toString() {
        return "Policy Components Factory";
    }

}
