package org.protege.owl.server.util;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerComponentFactory;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;

public abstract class ServerComponentFactoryAdapter implements ServerComponentFactory {

    @Override
    public boolean hasSuitableServer(OWLIndividual i) {
        return false;
    }

    @Override
    public Server createServer(OWLIndividual i) {
        throw new IllegalStateException("This server component factory does not create this type of object");
    }

    @Override
    public boolean hasSuitableServerFilter(OWLIndividual i) {
        return false;
    }

    @Override
    public ServerFilter createServerFilter(OWLIndividual i, Server server) {
        throw new IllegalStateException("This server component factory does not create this type of object");
    }

    @Override
    public boolean hasSuitableServerTransport(OWLIndividual i) {
        return false;
    }

    @Override
    public ServerTransport createServerTransport(OWLIndividual i) {
        throw new IllegalStateException("This server component factory does not create this type of object");
    }

}
