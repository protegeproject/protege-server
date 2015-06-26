package org.protege.owl.server.experiments;

import static org.protege.owl.server.TestUtilities.REDMOND;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import junit.framework.Assert;

import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerTransport;
import org.protege.owl.server.connect.local.LocalClient;
import org.protege.owl.server.connect.local.LocalTransport;
import org.protege.owl.server.connect.local.LocalTransportImpl;
import org.protege.owl.server.core.ServerImpl;
import org.protege.owl.server.policy.UnauthorizedToken;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestBadUpdate {
    private LocalTransport transport;

    @BeforeMethod
    public void startServer() throws IOException {
        Server server = new ServerImpl(new File("src/test/resources/root.04"), new File("build/server/configuration"));
        transport = new LocalTransportImpl();
        transport.start(server);
        server.setTransports(Collections.singleton((ServerTransport) transport));
    }
    
    @Test
    public void testBadUpdate() throws OWLServerException, OWLOntologyCreationException {   
        Client client = transport.getClient(new UnauthorizedToken(REDMOND.getUsername()));
        RemoteOntologyDocument doc = (RemoteOntologyDocument) client.getServerDocument(IRI.create(LocalClient.SCHEME + "://localhost/Pizza.history"));
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        VersionedOntologyDocument vont = ClientUtilities.loadOntology(client, manager, doc);
        Assert.assertEquals(0, ClientUtilities.getUncommittedChanges(client, vont).size());
        ClientUtilities.update(client, vont, new OntologyDocumentRevision(1).asPointer());
        Assert.assertEquals(0, ClientUtilities.getUncommittedChanges(client, vont).size());
    }
}
