package org.protege.owl.server.command;

import static org.protege.owl.server.command.P4OWLServerOptions.NEEDS_HELP_OPTION;
import static org.protege.owl.server.command.P4OWLServerOptions.ONTOLOGY_FORMAT_OPTION;
import static org.protege.owl.server.command.P4OWLServerOptions.REVISION_OPTION;

import java.io.Console;
import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.exception.AuthenticationFailedException;
import org.protege.owl.server.api.exception.ConflictException;
import org.protege.owl.server.connect.rmi.RMIClient;
import org.protege.owl.server.util.ClientRegistry;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyFormat;

public abstract class ServerCommand {
    private ClientRegistry registry;
    private boolean needsHelp = false;
    
    public ServerCommand() {
        registry = new ClientRegistry();
        registry.addFactory(new CRMIClientFactory());
    }
    
    public void run(String[] args) throws Exception {
        try {
            if (parse(args) && !needsHelp()) {
                execute();
            }
            else {
                usage();
            }
        }
        catch (ParseException pe) {
            System.out.println("Could not parse the command line");
            pe.printStackTrace();
            usage();
        }
        catch (AuthenticationFailedException afe) {
            System.out.println("Authentication failed.");
        }
        catch (ConflictException ce) {
            System.out.println("Conflict with another user detected.");
        }
    }
    
    public ClientRegistry getClientRegistry() {
        return registry;
    }
    
    protected void loadCommandLine(CommandLine cmd) {
        needsHelp = cmd.hasOption(NEEDS_HELP_OPTION.getArgName()) || cmd.hasOption(NEEDS_HELP_OPTION.getLongOpt());
    }
    
    protected File parseSingleExistingFile(String[] args, Options options) throws ParseException {
        File f = null;
        CommandLine cmd = new GnuParser().parse(options, args, true);
        loadCommandLine(cmd);
        String[] remainingArgs = cmd.getArgs();
        if (!needsHelp() && remainingArgs.length == 1) {
            f = new File(remainingArgs[0]);
        }
        if (f != null && !f.exists()) {
            System.out.println("File " + f + " not found.");
            f = null;
        }
        return f;
    }
    
    protected OWLOntologyFormat parseFormat(CommandLine cmd) {
        OWLOntologyFormat format = new RDFXMLOntologyFormat();
        String formatString = cmd.getOptionValue(ONTOLOGY_FORMAT_OPTION.getArgName());
        if (formatString == null) {
            formatString = cmd.getOptionValue(ONTOLOGY_FORMAT_OPTION.getLongOpt());
        }
        if (formatString == null) {
            ;
        }
        else if (formatString.equals("owlxml")) {
            format = new OWLXMLOntologyFormat();
        }
        else if (formatString.equals("rdfxml")) {
            format = new RDFXMLOntologyFormat();
        }
        else if (formatString.equals("functional")) {
            format = new OWLFunctionalSyntaxOntologyFormat();
        }
        else {
            System.out.println("Unknown format type " + formatString);
        }
        return format;
    }
    
    protected RevisionPointer parseRevision(CommandLine cmd) {
        RevisionPointer rp = RevisionPointer.HEAD_REVISION;
        String formatString = cmd.getOptionValue(REVISION_OPTION.getArgName());
        if (formatString == null) {
            formatString = cmd.getOptionValue(REVISION_OPTION.getLongOpt());
        }
        if (formatString == null) {
            ;
        }
        else {
            int revision = Integer.parseInt(formatString);
            rp = new OntologyDocumentRevision(revision).asPointer();
        }
        return rp;
    }
    
    protected ChangeMetaData getCommitComment() {
        Console console = System.console();
        console.writer().println("Write commit comment here and end with an empty line.");
        console.writer().flush();
        StringBuffer sb = new StringBuffer();
        boolean firstTime = true;
        for (String line = console.readLine(); line != null && !line.isEmpty(); line = console.readLine()) {
            if (firstTime) {
                firstTime = false;
            }
            else {
                sb.append('\n');
            }
            sb.append(line);
        }
        return new ChangeMetaData(sb.toString());
    }
    
    protected void usage(String shortUsage, String footer, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(80, shortUsage, "", options, footer);
    }
    
    public boolean needsHelp() {
        return needsHelp;
    }
    
    protected String showFormats() {
        return "Available Ontology formats are rdfxml, owlxml, functional";   
    }
    
    protected String showIRI() {
        return "A typical server IRI looks like this: " + RMIClient.SCHEME + "://tania12g:4875/";
    }
    
    public abstract boolean parse(String[] args) throws ParseException;

    public abstract void execute() throws Exception;
    
    public abstract void usage();
    
}
