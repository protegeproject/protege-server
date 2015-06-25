package org.protege.owl.server.command;

import static org.protege.owl.server.command.P4OWLServerOptions.AXIOM_COUNT_OPTION;
import static org.protege.owl.server.command.P4OWLServerOptions.NEEDS_HELP_OPTION;
import static org.protege.owl.server.command.P4OWLServerOptions.REVISION_RANGE_OPTION;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.render.DiffRenderer;
import org.protege.owl.server.util.ClientRegistry;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Log extends ServerCommand {
    private Options options = new Options();
    {
        options.addOption(AXIOM_COUNT_OPTION);
        options.addOption(NEEDS_HELP_OPTION);
        options.addOption(REVISION_RANGE_OPTION);
    }
    private File ontologyFile;
    private IRI serverLocation;
    private RevisionPointer[] range;
    private int axiomCount = -1;
    private Client client;
    private RemoteOntologyDocument remoteDoc;


    @Override
    public boolean parse(String[] args) throws ParseException {
        CommandLine cmd = new GnuParser().parse(options, args, true);
        loadCommandLine(cmd);
        String[] remainingArgs = cmd.getArgs();
        if (remainingArgs.length == 1) {
            File testFile = new File(remainingArgs[0]);
            if (testFile.exists()) {
                ontologyFile = testFile;
            }
            else {
                serverLocation = IRI.create(remainingArgs[0]); 
            }
        }
        return remainingArgs.length == 1;
    }
    
    @Override
    protected void loadCommandLine(CommandLine cmd) {
        super.loadCommandLine(cmd);
        range = parseRevisionRange(cmd);
        axiomCount = parseAxiomCount(cmd);
    }

    @Override
    public void execute() throws Exception {
        client = null;
        connect();
        if (client != null) {
            ChangeHistory history = client.getChanges(remoteDoc, range[0], range[1]);
            displayHistory(history);
        }
        else {
            System.out.println("Could not connect to server.");
        }
    }
    
    private void connect() throws OWLOntologyCreationException, OWLServerException, IOException {
        ClientRegistry registry = getClientRegistry();
        if (ontologyFile != null) {
            connectUsingFile(registry);
        }
        else {
            connectToIRI(registry);
        }
    }
   
    private void connectUsingFile(ClientRegistry registry) throws OWLOntologyCreationException, OWLServerException, IOException {
        IRI ontologyDocumentLocation = IRI.create(ontologyFile);
        if (registry.hasServerMetadata(ontologyDocumentLocation)) {
            IRI serverLocation = registry.getServerLocation(ontologyDocumentLocation);
            if (registry.isSuitable(serverLocation)) {
                client = registry.connectToServer(serverLocation);
                remoteDoc = (RemoteOntologyDocument) client.getServerDocument(serverLocation);
            }
        }
    }
    
    
    private void connectToIRI(ClientRegistry registry) throws OWLServerException {
        if (registry.isSuitable(serverLocation)) {
            client = registry.connectToServer(serverLocation);
            remoteDoc = (RemoteOntologyDocument) client.getServerDocument(serverLocation);
        }
    }
    
    private void displayHistory(ChangeHistory history) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology();
        System.out.println("Showing logs for " + remoteDoc);
        DiffRenderer renderer = new DiffRenderer();
        for (OntologyDocumentRevision revision = history.getStartRevision(); revision.compareTo(history.getEndRevision()) < 0; revision = revision.next()) {
            ChangeMetaData metaData = history.getMetaData(revision);
            System.out.println("From revision " + revision + " to revision " + revision.next() + ", User = " + metaData.getUser());
            System.out.println("\tCommit Comment:\n");
            System.out.println(metaData.getCommitComment());
            if (axiomCount > 0) {
                System.out.println("\n\tChanges:\n");
                ChangeHistory oneStepHistory = history.cropChanges(revision, revision.next());
                renderer.renderDiff(oneStepHistory.getChanges(ontology), client.getDocumentFactory().getOWLRenderer(), new PrintWriter(System.out), axiomCount);
            }
        }
    }

    @Override
    public void usage() {
        usage("Log <options> ontology-file/server-IRI", "", options);
    }

    /**
     * @param args	args
     * @throws Exception	Exception
     */
    public static void main(String[] args) throws Exception {
        new Log().run(args);
    }

}
