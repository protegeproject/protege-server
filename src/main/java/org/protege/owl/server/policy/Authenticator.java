package org.protege.owl.server.policy;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

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
import org.semanticweb.owlapi.model.IRI;

public class Authenticator extends ServerFilter {
    
    public Authenticator(Server delegate) {
        super(delegate);
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
