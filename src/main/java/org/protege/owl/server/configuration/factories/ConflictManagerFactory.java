package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.BASIC_CONFLICT_MANAGER;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.conflict.ConflictManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class ConflictManagerFactory implements ServerComponentFactory {
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
        return ontology.containsAxiom(factory.getOWLClassAssertionAxiom(BASIC_CONFLICT_MANAGER, i));
    }

    @Override
    public ServerFilter createServerFilter(OWLIndividual i, Server server) {
        if (!hasSuitableServerFilter(i)) {
            throw new IllegalStateException("Can't construct this filter" + i);
        }
        return new ConflictManager(server);
    }

    @Override
    public boolean hasSuitableServerTransport(OWLIndividual i) {
        return false;
    }

    @Override
    public ServerTransport createServerTransport(OWLIndividual i) {
        return null;
    }
    
    @Override
    public String toString() {
        return "Basic Conflict Manager Factory";
    }

}
