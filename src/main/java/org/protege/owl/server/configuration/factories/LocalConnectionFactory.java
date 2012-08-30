package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.LOCAL_TRANSPORT;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.connect.local.LocalTransportImpl;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class LocalConnectionFactory implements ServerComponentFactory {
    private OWLOntology ontology;
    private OWLDataFactory factory;

    @Override
    public void setConfiguration(OWLOntology ontology) {
        this.ontology = ontology;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
    }

    @Override
    public boolean hasSuitableServer(OWLIndividual i) {
        return false;
    }

    @Override
    public Server createServer(OWLIndividual i) {
        throw new IllegalStateException("This call is not valid for this factory");
    }

    @Override
    public boolean hasSuitableServerFilter(OWLIndividual i) {
        return false;
    }

    @Override
    public ServerFilter createServerFilter(OWLIndividual i, Server server) {
        throw new IllegalStateException("This call is not valid for this factory");
    }

    @Override
    public boolean hasSuitableServerTransport(OWLIndividual i) {
        return ontology.containsAxiom(factory.getOWLClassAssertionAxiom(LOCAL_TRANSPORT, i));
    }

    @Override
    public ServerTransport createServerTransport(OWLIndividual i) {
        return new LocalTransportImpl();
    }

}
