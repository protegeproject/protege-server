package org.protege.owl.server.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.ServerOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;

public class ServerFilterAdapter extends ServerFilter {
    
    public ServerFilterAdapter(Server delegate) {
        super(delegate);
    }

    @Override
    public OntologyDocumentRevision evaluateRevisionPointer(AuthToken u, ServerOntologyDocument doc, RevisionPointer pointer) throws OWLServerException {
        return getDelegate().evaluateRevisionPointer(u, doc, pointer);
    }
    
    @Override
    public ServerDocument getServerDocument(AuthToken u, ServerPath servePath) throws OWLServerException {
        return getDelegate().getServerDocument(u, servePath);
    }

    @Override
    public Collection<ServerDocument> list(AuthToken u, ServerDirectory dir) throws OWLServerException {
        return getDelegate().list(u, dir);
    }

    @Override
    public ServerDirectory createDirectory(AuthToken u, ServerPath serverPath) throws OWLServerException {
        return getDelegate().createDirectory(u, serverPath);
    }

    @Override
    public ServerOntologyDocument createOntologyDocument(AuthToken u, ServerPath serverPath, Map<String, Object> settings) throws OWLServerException {
        return getDelegate().createOntologyDocument(u, serverPath, settings);
    }

    @Override
    public ChangeHistory getChanges(AuthToken u, ServerOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException {
        return getDelegate().getChanges(u, doc, start, end);
    }

    @Override
    public void commit(AuthToken u, ServerOntologyDocument doc, 
                        ChangeHistory changes) throws OWLServerException {
        getDelegate().commit(u, doc, changes);
    }

    @Override
    public void shutdown() {
        getDelegate().shutdown();
    }

    @Override
    public InputStream getConfigurationInputStream(String fileName) throws OWLServerException {
        return getDelegate().getConfigurationInputStream(fileName);
    }
    
    @Override
    public OutputStream getConfigurationOutputStream(String fileName) throws OWLServerException {
        return getDelegate().getConfigurationOutputStream(fileName);
    }

    @Override
    public InputStream getConfigurationInputStream(ServerDocument doc, String extension) throws OWLServerException {
        return getDelegate().getConfigurationInputStream(doc, extension);
    }
    
    @Override
    public OutputStream getConfigurationOutputStream(ServerDocument doc, String extension) throws OWLServerException {
        return getDelegate().getConfigurationOutputStream(doc, extension);
    }

    @Override
    public void setTransports(Collection<ServerTransport> transports) {
        getDelegate().setTransports(transports);
    }

    @Override
    public Collection<ServerTransport> getTransports() {
        return getDelegate().getTransports();
    }
    
    @Override
    public DocumentFactory getDocumentFactory() {
        return getDelegate().getDocumentFactory();
    }
    

}
