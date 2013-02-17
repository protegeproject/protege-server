package org.protege.owl.server.command;

import static org.protege.owl.server.command.P4OWLServerOptions.NEEDS_HELP_OPTION;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class Upload extends ServerCommand {
    private Options options = new Options();
    {
        options.addOption(NEEDS_HELP_OPTION);
    }
    
    private IRI serverIRI;
    private File fileToUpload;
    
    @Override
    public boolean parse(String[] args) throws ParseException {
        boolean goForIt = false;
        CommandLine cmd = new GnuParser().parse(options, args, true);
        String[] remainingArgs = cmd.getArgs();
        if (!needsHelp() && remainingArgs.length == 2) {
            fileToUpload = new File(remainingArgs[0]);
            String iriString = remainingArgs[1];
            serverIRI = IRI.create(iriString);
            goForIt = true;
        }
        if (fileToUpload != null && !fileToUpload.exists()) {
            System.out.println("File to upload, " + fileToUpload + ", does not exist.");
            goForIt = false;
        }
        return goForIt;
    }
    
    @Override
    public void execute() throws OWLServerException, OWLOntologyCreationException, OWLOntologyStorageException, IOException {
        Client client = getClientRegistry().connectToServer(serverIRI);
        if (client != null) {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(fileToUpload);
            ChangeMetaData metaData = getCommitComment();
            ClientUtilities.createServerOntology(client, serverIRI, metaData, ontology);
            System.out.println("Upload complete.");
        }
        else {
            System.out.println("Could not open connection to client");
        }
    }
    
    public void usage() {
        usage("Upload <options> fileToUpload serverIRI", showIRI(), options);
    }

    /**
     * @param args
     * @throws OWLServerException 
     * @throws OWLOntologyCreationException 
     * @throws OWLOntologyStorageException 
     * @throws ParseException 
     * @throws IOException 
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception  {
        new Upload().run(args);
    }

}
