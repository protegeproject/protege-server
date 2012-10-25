package org.protege.owl.server.command;

import static org.protege.owl.server.command.P4OWLServerOptions.NEEDS_HELP_OPTION;

import java.io.Console;
import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.render.DiffRenderer;
import org.protege.owl.server.util.ClientRegistry;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Uncommitted extends ServerCommand {
    private Options options = new Options();
    {
        options.addOption(NEEDS_HELP_OPTION);
    }
    private File ontologyFile;

    @Override
    public boolean parse(String[] args) throws ParseException {
        boolean goForIt = false;
        CommandLine cmd = new GnuParser().parse(options, args, true);
        String[] remainingArgs = cmd.getArgs();
        if (!needsHelp(cmd) && remainingArgs.length == 1) {
            ontologyFile = new File(remainingArgs[0]);
            goForIt = true;
        }
        if (ontologyFile != null && !ontologyFile.exists()) {
            System.out.println("File " + ontologyFile + " not found and cannot be analyzed.");
            goForIt = false;
        }
        return goForIt;
    }

    @Override
    public void execute() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntology(IRI.create(ontologyFile));
        ClientRegistry registry = getClientRegistry();
        if (registry.hasSuitableMetaData(ontology)) {
            Client client = registry.connectToServer(ontology);
            VersionedOntologyDocument vont = registry.getVersionedOntologyDocument(ontology);
            List<OWLOntologyChange> changes = ClientUtilities.getUncommittedChanges(client, vont);
            Console console = System.console();
            new DiffRenderer().renderDiff(changes, client.getDocumentFactory().getOWLRenderer(), console.writer(), -1);
        }
        else {
            System.out.println("Could not connect to appropriate server - no known server metadata found.");
        }
    }

    @Override
    public void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(80, "Uncommitted <options> ontology-file", "", options, "");
    }

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        new Uncommitted().run(args);
    }

}
