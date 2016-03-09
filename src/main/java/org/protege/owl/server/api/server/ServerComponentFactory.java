package org.protege.owl.server.api.server;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

@Deprecated
public interface ServerComponentFactory {

    @Deprecated
    void setConfiguration(OWLOntology ontology);

    @Deprecated
    boolean hasSuitableServer(OWLIndividual i);

    @Deprecated
    Server createServer(OWLIndividual i);

    @Deprecated
    boolean hasSuitableServerFilter(OWLIndividual i);

    @Deprecated
    ServerFilter createServerFilter(OWLIndividual i, Server server);

    @Deprecated
    boolean hasSuitableServerTransport(OWLIndividual i);

    @Deprecated
    ServerTransport createServerTransport(OWLIndividual i);
}
