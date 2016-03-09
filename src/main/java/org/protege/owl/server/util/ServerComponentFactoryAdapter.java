package org.protege.owl.server.util;

import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerComponentFactory;
import org.protege.owl.server.api.server.ServerFilter;
import org.protege.owl.server.api.server.ServerTransport;
import org.semanticweb.owlapi.model.OWLIndividual;

@Deprecated
public abstract class ServerComponentFactoryAdapter implements ServerComponentFactory {

    @Deprecated
    @Override
    public boolean hasSuitableServer(OWLIndividual i) {
        return false;
    }

    @Deprecated
    @Override
    public Server createServer(OWLIndividual i) {
        throw new IllegalStateException("This server component factory does not create this type of object");
    }

    @Deprecated
    @Override
    public boolean hasSuitableServerFilter(OWLIndividual i) {
        return false;
    }

    @Deprecated
    @Override
    public ServerFilter createServerFilter(OWLIndividual i, Server server) {
        throw new IllegalStateException("This server component factory does not create this type of object");
    }

    @Deprecated
    @Override
    public boolean hasSuitableServerTransport(OWLIndividual i) {
        return false;
    }

    @Deprecated
    @Override
    public ServerTransport createServerTransport(OWLIndividual i) {
        throw new IllegalStateException("This server component factory does not create this type of object");
    }

}
