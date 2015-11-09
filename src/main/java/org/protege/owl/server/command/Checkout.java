package org.protege.owl.server.command;

import static org.protege.owl.server.command.P4OWLServerOptions.NEEDS_HELP_OPTION;
import static org.protege.owl.server.command.P4OWLServerOptions.ONTOLOGY_FORMAT_OPTION;
import static org.protege.owl.server.command.P4OWLServerOptions.REVISION_OPTION;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.RemoteOntologyDocument;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class Checkout extends ServerCommand {
    private Options options = new Options();
    {
        options.addOption(NEEDS_HELP_OPTION);
        options.addOption(ONTOLOGY_FORMAT_OPTION);
        options.addOption(REVISION_OPTION);
    }
    
    private IRI serverIRI;
    private File savedLocation;
    private OWLOntologyFormat format;
    RevisionPointer revision = RevisionPointer.HEAD_REVISION;
    
    @Override
    public boolean parse(String[] args) throws ParseException {
        try {
            boolean goForIt = false;
            CommandLine cmd = new GnuParser().parse(options, args, true);
            loadCommandLine(cmd);
            String[] remainingArgs = cmd.getArgs();
            if (!needsHelp() && remainingArgs.length == 2) {
                String iriString = remainingArgs[0];
                serverIRI = IRI.create(iriString);
                savedLocation = new File(remainingArgs[1]);
                goForIt = true;
            }
            if (savedLocation != null && savedLocation.exists()) {
                System.out.println("File " + savedLocation + " already exists.");
                System.out.println("Overwrite?");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String response = reader.readLine().toLowerCase();
                goForIt = response.equals("y") || response.equals("yes");
            }
            return goForIt;
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    
    @Override
    protected void loadCommandLine(CommandLine cmd) {
        super.loadCommandLine(cmd);
        format = parseFormat(cmd);
        revision = parseRevision(cmd);
    }
    
    @Override
    public void execute() throws OWLServerException, OWLOntologyCreationException, OWLOntologyStorageException, IOException {
        Client client = getClientRegistry().connectToServer(serverIRI);
        if (client != null) {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            RemoteOntologyDocument doc = (RemoteOntologyDocument) client.getServerDocument(serverIRI);
            VersionedOntologyDocument vont = ClientUtilities.loadOntology(client, manager, doc, revision);
            IRI ontologyIRI = IRI.create(savedLocation);
            manager.saveOntology(vont.getOntology(), format, ontologyIRI);
            System.out.println("Ontology saved to " + savedLocation);
            manager.setOntologyDocumentIRI(vont.getOntology(), ontologyIRI);
            if (!vont.saveMetaData()) {
                System.out.println("Ontology saved but meta data was not included.");
            }
        }
        else {
            System.out.println("Could not open connection to client");
        }
    }
    
    public void usage() {
        usage("Checkout <options> serverIRI savedLocation", showFormats() + "\n" + showIRI(), options);
    }

    /**
     * @param args	args
     * @throws OWLServerException	OWLServerException
     * @throws OWLOntologyCreationException	OWLOntologyCreationException
     * @throws OWLOntologyStorageException	OWLOntologyStorageException
     * @throws ParseException	ParseException
     * @throws IOException	IOException
     * @throws Exception	Exception
     */
    public static void main(String[] args) throws Exception  {
        new Checkout().run(args);
    }

}
