package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.List;

import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.ClientFactory;
import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;

public class ClientRegistry implements ClientFactory {
    private List<ClientFactory> factories = new ArrayList<ClientFactory>();
    
    public void addFactory(ClientFactory factory) {
        factories.add(factory);
    }

    @Override
    public boolean isSuitable(IRI serverLocation) {
        for (ClientFactory factory : factories) {
            if (factory.isSuitable(serverLocation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Client createClient(IRI serverLocation) throws OWLServerException {
        for (ClientFactory factory : factories) {
            if (factory.isSuitable(serverLocation)) {
                return factory.createClient(serverLocation);
            }
        }
        return null;
    }

}
