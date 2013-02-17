package org.protege.owl.server.command;

import static org.protege.owl.server.command.P4OWLServerOptions.NEEDS_HELP_OPTION;
import static org.protege.owl.server.command.P4OWLServerOptions.REVISION_OPTION;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.client.Client;
import org.protege.owl.server.api.client.VersionedOntologyDocument;
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
        ontologyFile = parseSingleExistingFile(args, options);
        return ontologyFile != null && ontologyFile.isFile();
    }
    
    @Override
    protected void loadCommandLine(CommandLine cmd) {
        super.loadCommandLine(cmd);
        revision = parseRevision(cmd);
    }

    @Override
    public void execute() throws OWLOntologyCreationException, IOException, OWLServerException, OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntology(IRI.create(ontologyFile));
        ClientRegistry registry = getClientRegistry();
        if (registry.hasSuitableMetaData(ontology)) {
            Client client = registry.connectToServer(ontology);
            VersionedOntologyDocument vont = registry.getVersionedOntologyDocument(ontology);
            System.out.println("Current Revision = " + vont.getRevision());
            ClientUtilities.update(client, vont, revision);
            manager.saveOntology(ontology);
            vont.saveMetaData();
            System.out.println("Update completed to revision " + vont.getRevision());
        }
        else {
            System.out.println("Could not connect to appropriate server - no known server metadata found.");
        }
    }

    @Override
    public void usage() {
        usage("Update <options> ontology-file", "", options);
    }

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        new Update().run(args);
    }

}
