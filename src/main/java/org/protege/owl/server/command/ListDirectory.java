package org.protege.owl.server.command;

import static org.protege.owl.server.command.P4OWLServerOptions.NEEDS_HELP_OPTION;

import java.util.Collection;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.RemoteServerDocument;
import org.semanticweb.owlapi.model.IRI;

public class ListDirectory extends ServerCommand {
    private Options options = new Options();
    {
        options.addOption(NEEDS_HELP_OPTION);
    }
    private IRI serverLocation;

    @Override
    public boolean parse(String[] args) throws ParseException {
        CommandLine cmd = new GnuParser().parse(options, args, true);
        String[] remainingArgs = cmd.getArgs();
        if (!needsHelp(cmd) && remainingArgs.length == 1) {
            serverLocation = IRI.create(remainingArgs[0]);
        }
        return serverLocation != null;
    }

    @Override
    public void execute() throws Exception {
        Client client = getClientRegistry().connectToServer(serverLocation);
        if (client == null) {
            System.out.println("Could not connect to remote server");
        }
        RemoteServerDirectory dir = (RemoteServerDirectory) client.getServerDocument(serverLocation);
        Collection<RemoteServerDocument> docs = client.list(dir);
        if (!docs.isEmpty()) {
            TreeSet<RemoteServerDocument> sortedDocs = new TreeSet<RemoteServerDocument>(docs);
            for (RemoteServerDocument doc : sortedDocs) {
                String type = doc instanceof RemoteOntologyDocument ? "O - " : "D - ";
                System.out.println(type + doc.getServerLocation());
            }
        }
        else {
            System.out.println("No documents found");
        }
    }

    @Override
    public void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(80, "List <options> server-directory-irib", "", options, "");
    }

    /**
     * @param args
     */
    public static void main(String[] args)  throws Exception {
        new ListDirectory().run(args);
    }

}
