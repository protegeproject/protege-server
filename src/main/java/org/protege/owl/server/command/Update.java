package org.protege.owl.server.command;

import static org.protege.owl.server.command.P4OWLServerOptions.NEEDS_HELP_OPTION;
import static org.protege.owl.server.command.P4OWLServerOptions.REVISION_OPTION;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.util.ClientRegistry;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


public class Update extends ServerCommand {
    private Options options = new Options();
    {
        options.addOption(NEEDS_HELP_OPTION);
        options.addOption(REVISION_OPTION);
    }
    private File ontologyFile;
    private RevisionPointer revision;

    @Override
    public boolean parse(String[] args) throws ParseException {
        boolean goForIt = false;
        CommandLine cmd = new GnuParser().parse(options, args, true);
        revision = parseRevision(cmd);
        String[] remainingArgs = cmd.getArgs();
        if (!needsHelp(cmd) && remainingArgs.length == 1) {
            ontologyFile = new File(remainingArgs[0]);
            goForIt = true;
        }
        if (ontologyFile != null && !ontologyFile.exists()) {
            System.out.println("File " + ontologyFile + " not found and cannot be updated.");
            goForIt = false;
        }
        return goForIt;
    }

    @Override
    public void execute() throws OWLOntologyCreationException, IOException, OWLServerException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntology(IRI.create(ontologyFile));
        ClientRegistry registry = getClientRegistry();
        if (registry.hasSuitableMetaData(ontology)) {
            Client client = registry.connectToServer(ontology);
            VersionedOntologyDocument vont = registry.getVersionedOntologyDocument(ontology);
            ClientUtilities.update(client, vont, revision);
            manager.saveOntology(ontology);
            vont.saveMetaData();
            System.out.println("Update complete.");
        }
        else {
            System.out.println("Could not connect to appropriate server - no known server metadata found.");
        }
    }

    @Override
    public void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(80, "Update <options> ontology-file", "", options, "");
    }

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        new Update().run(args);
    }

}
