package org.protege.owl.server.connect.rmi;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ClientFactory;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.AuthenticationFailedException;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.DocumentFactoryImpl;
import org.protege.owl.server.connect.RootUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public abstract class AbstractRMIClientFactory implements ClientFactory {
    private Map<IRI, AuthToken> authMap = new TreeMap<IRI, AuthToken>();
    private DocumentFactory factory = new DocumentFactoryImpl();
    
    @Override
    public boolean hasSuitableMetaData(OWLOntology ontology) throws IOException {
        if (factory.hasServerMetadata(ontology)) {
            return isSuitable(factory.getServerLocation(ontology));
        }
        return false;
    }
    
    @Override
    public VersionedOntologyDocument getVersionedOntologyDocument(OWLOntology ontology) throws IOException {
        return factory.getVersionedOntologyDocument(ontology);
    }
    
   @Override
   public RMIClient connectToServer(OWLOntology ontology) throws OWLServerException, IOException {
        return connectToServer(factory.getServerLocation(ontology));
    }

    @Override
    public boolean isSuitable(IRI serverLocation) {
        return serverLocation.getScheme().equals(RMIClient.SCHEME);
    }

    @Override
    public RMIClient connectToServer(IRI serverLocation) throws OWLServerException {
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
                    client = connectToServer(serverLocation);
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
