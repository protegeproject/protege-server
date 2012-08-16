package org.protege.owl.server.util;

import java.io.File;
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
import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;

public class ServerFilterAdapter extends ServerFilter {
    
    public ServerFilterAdapter(Server delegate) {
        super(delegate);
    }

    @Override
    public ServerDocument getServerDocument(User u, IRI serverIRI) throws OWLServerException {
        return getDelegate().getServerDocument(u, serverIRI);
    }

    @Override
    public Collection<ServerDocument> list(User u, ServerDirectory dir) throws OWLServerException {
        return getDelegate().list(u, dir);
    }

    @Override
    public ServerDirectory createDirectory(User u, IRI serverIRI) throws OWLServerException {
        return getDelegate().createDirectory(u, serverIRI);
    }

    @Override
    public RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws OWLServerException {
        return getDelegate().createOntologyDocument(u, serverIRI, settings);
    }

    @Override
    public ChangeDocument getChanges(User u, RemoteOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException {
        return getDelegate().getChanges(u, doc, start, end);
    }

    @Override
    public ChangeDocument commit(User u, RemoteOntologyDocument doc, ChangeMetaData commitComment, ChangeDocument changes, SortedSet<OntologyDocumentRevision> myCommits) throws OWLServerException {
        return getDelegate().commit(u, doc, commitComment, changes, myCommits);
    }

    @Override
    public void shutdown() {
        getDelegate().shutdown();
    }

    @Override
    public File getConfiguration(String fileName) throws OWLServerException {
        return getDelegate().getConfiguration(fileName);
    }

    @Override
    public File getConfiguration(ServerDocument doc, String extension) throws OWLServerException {
        return getDelegate().getConfiguration(doc, extension);
    }

    @Override
    public void setTransports(Collection<ServerTransport> transports) {
        getDelegate().setTransports(transports);
    }

    @Override
    public Collection<ServerTransport> getTransports() {
        return getDelegate().getTransports();
    }
    

}
