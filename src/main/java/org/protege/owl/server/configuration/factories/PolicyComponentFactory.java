package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.BASIC_AUTHENTICATION_MANAGER;
import static org.protege.owl.server.configuration.MetaprojectVocabulary.BASIC_AUTHORIZATION_MANAGER;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.policy.Authenticator;
import org.protege.owl.server.policy.PolicyFilter;
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
        return hasSuitableAuthenticationFilter(i) || hasSuitableAuthorizationFilter(i);
    }
    
    private boolean hasSuitableAuthenticationFilter(OWLIndividual i) {
        OWLAxiom hasRightType = factory.getOWLClassAssertionAxiom(BASIC_AUTHENTICATION_MANAGER, i);
        return metaOntology.containsAxiom(hasRightType); 
    }
    
    private boolean hasSuitableAuthorizationFilter(OWLIndividual i) {
        OWLAxiom hasRightType = factory.getOWLClassAssertionAxiom(BASIC_AUTHORIZATION_MANAGER, i);
        return metaOntology.containsAxiom(hasRightType); 
    }

    @Override
    public ServerFilter createServerFilter(OWLIndividual i, Server server) {
        try {
            if (hasSuitableAuthenticationFilter(i)) {
                return new Authenticator(server);
            }
            else if (hasSuitableAuthorizationFilter(i)) {
                return new PolicyFilter(server);
            }
            else {
                throw new IllegalArgumentException("Invalid call to policy component factory.");
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Factory failed to setup server", e);
        }
    }
    
    public String toString() {
        return "Policy Components Factory";
    }

}
