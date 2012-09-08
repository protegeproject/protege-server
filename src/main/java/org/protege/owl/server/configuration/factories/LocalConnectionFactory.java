package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.LOCAL_TRANSPORT;

import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.connect.local.LocalTransportImpl;
import org.protege.owl.server.util.ServerComponentFactoryAdapter;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class LocalConnectionFactory extends ServerComponentFactoryAdapter {
    private OWLOntology ontology;
    private OWLDataFactory factory;

    @Override
    public void setConfiguration(OWLOntology ontology) {
        this.ontology = ontology;
        this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
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
