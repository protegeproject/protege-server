package org.protege.owl.server.command;

import java.io.File;

import org.protege.owl.server.util.ClientRegistry;
import org.semanticweb.owlapi.model.IRI;

public class Main {
    private ClientRegistry registry;
    private String[] args;

    public Main(String[] args) {
        this.args = args;
        initializeRegistry();
    }
    
    private void initializeRegistry() {
        registry = new ClientRegistry();
        registry.addFactory(new CRMIClientFactory());
    }
    
    public void run() {
        if ("upload".equals(args[0])) {
            upload(args);
        }
        else if ("list".equals(args[0])) {
            list(args);
        }
        else if ("mkdir".equals(args[0])) {
            mkdir(args);
        }
        else if ("checkout".equals(args[0])) {
            // checkout, update and commit are the obvious remaining guys
        }
        else {
            usage();
        } 
    }
    
    private void mkdir(String[] args) {
        new Mkdir(registry, IRI.create(args[1])).run();
    }
    
    private void list(String[] args) {
        new List(registry, IRI.create(args[2])).run();
    }
    
    private void upload(String[] args) {
        new Upload(registry, new File(args[1]), IRI.create(args[2])).run();
    }
    
    private void usage() {
        System.out.println("Usage:");
        System.out.println("\tupload file server-iri");
    }
    
    /**
     * @param args
     */
    public static void main(String args[]) {
        new Main(args).run();
    }
    

}
