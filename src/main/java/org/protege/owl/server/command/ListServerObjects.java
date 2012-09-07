package org.protege.owl.server.command;

import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.RemoteServerDocument;
import org.protege.owl.server.util.ClientRegistry;
import org.semanticweb.owlapi.model.IRI;

public class ListServerObjects implements Runnable {
    private ClientRegistry registry;
    private IRI serverLoc;
    
    public ListServerObjects(ClientRegistry registry, IRI serverLoc) {
        this.registry = registry;
        this.serverLoc = serverLoc;
    }

    @Override
    public void run() {
        try {
            Client client = registry.connectToServer(serverLoc);
            RemoteServerDocument doc = client.getServerDocument(serverLoc);
            if (doc instanceof RemoteServerDirectory) {
                for (RemoteServerDocument innerDoc : client.list((RemoteServerDirectory) doc)) {
                    String fragment = innerDoc.getServerLocation().getFragment().toString();
                    if (innerDoc instanceof RemoteServerDirectory) {
                        System.out.println(fragment + "/");
                    }
                    else {
                        System.out.println(fragment);
                    }
                }
            }
            else {
                System.out.println("" + serverLoc + " is not a directory");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
