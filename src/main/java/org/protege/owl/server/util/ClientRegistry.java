package org.protege.owl.server.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.ClientFactory;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.api.VersionedOntologyDocument;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public class ClientRegistry implements ClientFactory {
    private List<ClientFactory> factories = new ArrayList<ClientFactory>();
    
    @Override
    public boolean hasSuitableMetaData(OWLOntology ontology) throws IOException {
        for (ClientFactory factory : factories) {
            if (factory.hasSuitableMetaData(ontology)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public VersionedOntologyDocument getVersionedOntologyDocument(OWLOntology ontology) throws IOException {
        for (ClientFactory factory : factories) {
            if (factory.hasSuitableMetaData(ontology)) {
                return factory.getVersionedOntologyDocument(ontology);
            }
        }
        return null;
    }
    
    @Override
    public boolean hasServerMetadata(IRI ontologyDocumentLocation) {
        for (ClientFactory factory : factories) {
            if (factory.hasServerMetadata(ontologyDocumentLocation)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public IRI getServerLocation(IRI ontologyDocumentLocation) throws IOException {
        for (ClientFactory factory : factories) {
            if (factory.hasServerMetadata(ontologyDocumentLocation)) {
                return factory.getServerLocation(ontologyDocumentLocation);
            }
        }
        return null;
    }
    
    @Override
    public Client connectToServer(OWLOntology ontology) throws OWLServerException, IOException {
        for (ClientFactory factory : factories) {
            if (factory.hasSuitableMetaData(ontology)) {
                return factory.connectToServer(ontology);
            }
        }
        return null;
    }
    
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
    public Client connectToServer(IRI serverLocation) throws OWLServerException {
        for (ClientFactory factory : factories) {
            if (factory.isSuitable(serverLocation)) {
                return factory.connectToServer(serverLocation);
            }
        }
        return null;
    }
    
    @Override
    public Client connectToServer(IRI serverLocation, Properties info) throws OWLServerException {
        for (ClientFactory factory : factories) {
            if (factory.isSuitable(serverLocation)) {
                return factory.connectToServer(serverLocation, info);
            }
        }
        return null;
    }
    
    public Client connectToServer(IRI serverLocation, String username, String password) throws OWLServerException {
        Properties p = new Properties();
        p.setProperty(ClientFactory.USERNAME_KEY, username);
        p.setProperty(ClientFactory.PASSWORD_KEY, password);
        return connectToServer(serverLocation, p);
    }
    
    @Override
    public boolean hasReadyConnection(IRI serverLocation) {
        for (ClientFactory factory : factories) {
            if (factory.hasReadyConnection(serverLocation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<IRI> getReadyConnections() {
        Set<IRI> iris = new HashSet<IRI>();
        for (ClientFactory factory : factories) {
            iris.addAll(factory.getReadyConnections());
        }
        return iris;
    }
    
    @Override
    public Client quickConnectToServer(IRI serverLocation) {
        for (ClientFactory factory : factories) {
            if (factory.hasReadyConnection(serverLocation)) {
                return factory.quickConnectToServer(serverLocation);
            }
        }
        return null;
    }
}
