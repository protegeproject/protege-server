package org.protege.owl.server.command;

import java.io.File;

import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.util.ClientRegistry;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Upload implements Runnable {
    private ClientRegistry registry;
    private File f;
    private IRI serverLoc;
    
    public Upload(ClientRegistry registry, File f, IRI serverLoc) {
        this.registry = registry;
        this.f = f;
        this.serverLoc = serverLoc;
    }

    @Override
    public void run() {
        try {
            Client client = registry.connectToServer(serverLoc);
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            System.out.println("Loading ontology");
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
            System.out.println("Sending to Server");
            ClientUtilities.createServerOntology(client, serverLoc, new ChangeMetaData("Uploaded by command line client"), ontology);
            System.out.println("Uploaded");
        }
        catch (Exception e) {
           e.printStackTrace();
        }
    }

}
