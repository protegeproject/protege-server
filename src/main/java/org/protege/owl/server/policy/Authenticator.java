package org.protege.owl.server.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.api.User;
import org.protege.owl.server.api.exception.DocumentNotFoundException;
import org.protege.owl.server.policy.generated.UsersAndGroupsLexer;
import org.protege.owl.server.policy.generated.UsersAndGroupsParser;
import org.semanticweb.owlapi.model.IRI;

public class Authenticator extends ServerFilter {
    
    public Authenticator(Server delegate) throws IOException, RecognitionException {
        super(delegate);
        parse();
    }
    
    private void parse() throws IOException, RecognitionException {
        File usersAndGroups = getConfiguration("UsersAndGroups");
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(usersAndGroups));
        UsersAndGroupsLexer lexer = new UsersAndGroupsLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        UsersAndGroupsParser parser = new UsersAndGroupsParser(tokens);
        parser.top();
    }

    public ServerDocument getServerDocument(User u, IRI serverIRI) throws DocumentNotFoundException {
        return getDelegate().getServerDocument(u, serverIRI);
    }

    public Collection<ServerDocument> list(User u, ServerDirectory dir) throws IOException {
        return getDelegate().list(u, dir);
    }

    public ServerDirectory createDirectory(User u, IRI serverIRI) throws IOException {
        return getDelegate().createDirectory(u, serverIRI);
    }

    public RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws IOException {
        return getDelegate().createOntologyDocument(u, serverIRI, settings);
    }

    public ChangeDocument getChanges(User u, RemoteOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException {
        return getDelegate().getChanges(u, doc, start, end);
    }

    public ChangeDocument commit(User u, RemoteOntologyDocument doc, ChangeMetaData commitComment, ChangeDocument changes, SortedSet<OntologyDocumentRevision> myCommits) throws IOException {
        return getDelegate().commit(u, doc, commitComment, changes, myCommits);
    }

    public void shutdown() {
        getDelegate().shutdown();
    }

    public File getConfiguration(String fileName) throws IOException {
        return getDelegate().getConfiguration(fileName);
    }

    public File getConfiguration(ServerDocument doc, String extension) throws IOException {
        return getDelegate().getConfiguration(doc, extension);
    }

    public void setTransports(Collection<ServerTransport> transports) {
        getDelegate().setTransports(transports);
    }

    public Collection<ServerTransport> getTransports() {
        return getDelegate().getTransports();
    }

 

}
