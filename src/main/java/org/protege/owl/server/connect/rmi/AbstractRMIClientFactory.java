package org.protege.owl.server.connect.rmi;

import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.ClientFactory;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;

public abstract class AbstractRMIClientFactory implements ClientFactory {

    @Override
    public boolean isSuitable(IRI serverLocation) {
        return serverLocation.getScheme().equals(RMIClient.SCHEME);
    }

    @Override
    public Client createClient(IRI serverLocation) throws OWLServerException {
        try {
            RMIClient client = new RMIClient(login(serverLocation), serverLocation);
            client.initialise();
            return client;
        }
        catch (Exception e) {
            throw new OWLServerException(e);
        }
    }
    
    protected abstract AuthToken login(IRI serverLocation);

}
