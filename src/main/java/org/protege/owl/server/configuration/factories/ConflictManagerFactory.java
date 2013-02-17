package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.BASIC_CONFLICT_MANAGER;

import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerFilter;
import org.protege.owl.server.conflict.ConflictManager;
import org.protege.owl.server.util.ServerComponentFactoryAdapter;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class ConflictManagerFactory extends ServerComponentFactoryAdapter {
    private OWLOntology ontology;
    private OWLDataFactory factory;

    @Override
    public void setConfiguration(OWLOntology ontology) {
        this.ontology = ontology;
        factory = ontology.getOWLOntologyManager().getOWLDataFactory();
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
    public String toString() {
        return "Basic Conflict Manager Factory";
    }

}
