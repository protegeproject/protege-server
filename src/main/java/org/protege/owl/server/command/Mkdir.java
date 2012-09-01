package org.protege.owl.server.command;

import org.protege.owl.server.api.Client;
import org.protege.owl.server.util.ClientRegistry;
import org.semanticweb.owlapi.model.IRI;

public class Mkdir implements Runnable {
    private ClientRegistry registry;
    private IRI serverLoc;
    
    public Mkdir(ClientRegistry registry, IRI serverLoc) {
        this.registry = registry;
        this.serverLoc = serverLoc;
    }

    @Override
    public void run() {
        try {
            Client client = registry.connectToServer(serverLoc);
            client.createRemoteDirectory(serverLoc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
