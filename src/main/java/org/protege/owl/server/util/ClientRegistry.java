package org.protege.owl.server.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.ClientFactory;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
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
