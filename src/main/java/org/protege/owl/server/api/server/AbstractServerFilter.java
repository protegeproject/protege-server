package org.protege.owl.server.api.server;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.SingletonChangeHistory;
import org.protege.owl.server.api.exception.OWLServerException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import edu.stanford.protege.metaproject.api.AuthenticationDetails;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public abstract class AbstractServerFilter implements Server {

    private Server delegate;

    public AbstractServerFilter(Server delegate) {
        this.delegate = delegate;
    }

    public Server getDelegate() {
        return delegate;
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return getDelegate().getConfiguration();
    }

    /*
     * Deprecated classes
     */

    @Override
    @Deprecated
    public OntologyDocumentRevision evaluateRevisionPointer(AuthToken u, ServerOntologyDocument doc, RevisionPointer pointer) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public ServerDocument getServerDocument(AuthToken u, ServerPath serverIRI) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public void setTransports(Collection<ServerTransport> transports) {
        getDelegate().setTransports(transports);
    }

    @Override
    @Deprecated
    public Collection<ServerTransport> getTransports() {
        return getDelegate().getTransports();
    }

    @Override
    @Deprecated
    public Collection<ServerDocument> list(AuthToken u, ServerDirectory dir) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public Collection<ServerDocument> list(AuthenticationDetails token, ProjectId projectId) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public ServerDirectory createDirectory(AuthToken u, ServerPath serverIRI) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public ServerOntologyDocument createOntologyDocument(AuthToken u, ServerPath serverIRI, Map<String, Object> settings) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public ChangeHistory getChanges(AuthToken u, ServerOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public void commit(AuthToken u, ServerOntologyDocument doc, SingletonChangeHistory changes) throws OWLServerException {
    }

    @Override
    @Deprecated
    public DocumentFactory getDocumentFactory() {
        return null;
    }

    @Override
    @Deprecated
    public InputStream getConfigurationInputStream(String fileName) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public OutputStream getConfigurationOutputStream(String fileName) throws OWLServerException {
        return null;
    }

    @Override
    public InputStream getConfigurationInputStream(ServerDocument doc, String extension) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public OutputStream getConfigurationOutputStream(ServerDocument doc, String extension) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public void shutdown() {
    }

    @Override
    @Deprecated
    public void shutdown(AuthToken u) {
    }
}
