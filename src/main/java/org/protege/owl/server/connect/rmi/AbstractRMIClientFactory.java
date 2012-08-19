package org.protege.owl.server.connect.rmi;

import java.util.Map;
import java.util.TreeMap;

import javax.naming.AuthenticationException;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ClientFactory;
import org.protege.owl.server.api.exception.AuthenticationFailedException;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.connect.RootUtils;
import org.semanticweb.owlapi.model.IRI;

public abstract class AbstractRMIClientFactory implements ClientFactory {
    private Map<IRI, AuthToken> authMap = new TreeMap<IRI, AuthToken>(); 

    @Override
    public boolean isSuitable(IRI serverLocation) {
        return serverLocation.getScheme().equals(RMIClient.SCHEME);
    }

    @Override
    public RMIClient createClient(IRI serverLocation) throws OWLServerException {
        try {
            boolean existingAuthToken = true;
            IRI serverRoot = RootUtils.getRoot(serverLocation);
            AuthToken authToken = authMap.get(serverRoot);
            if (authToken == null) {
                authToken = login(serverLocation);
                authMap.put(serverRoot, authToken);
                existingAuthToken = false;
            }
            RMIClient client = new RMIClient(authToken, serverLocation);
            client.initialise();
            if (existingAuthToken) {
                try {
                    client.getServerDocument(serverRoot);
                }
                catch (AuthenticationFailedException ae) {
                    authMap.remove(serverRoot);
                    client = createClient(serverLocation);
                }
            }
            return client;
        }
        catch (Exception e) {
            throw new OWLServerException(e);
        }
    }
    
    protected abstract AuthToken login(IRI serverLocation);

}
