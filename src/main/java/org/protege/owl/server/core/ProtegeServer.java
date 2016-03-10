package org.protege.owl.server.core;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.SingletonChangeHistory;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerDirectory;
import org.protege.owl.server.api.server.ServerDocument;
import org.protege.owl.server.api.server.ServerListener;
import org.protege.owl.server.api.server.ServerOntologyDocument;
import org.protege.owl.server.api.server.ServerPath;
import org.protege.owl.server.api.server.ServerTransport;
import org.protege.owl.server.api.server.TransportHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.AuthenticationDetails;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * The main server that acts as the end-point server where user requests to the server
 * get implemented.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ProtegeServer implements Server {

    private ServerConfiguration configuration;

    private TransportHandler transport;

    private List<ServerListener> listeners = new ArrayList<ServerListener>();

    public ProtegeServer(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void addUser(AuthToken token, User newUser) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeUser(AuthToken token, UserId userId) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addProject(AuthToken token, Project newProject) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void viewProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void commit(AuthToken token, ProjectId projectId, CommitBundle changes) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTransport(TransportHandler transport) throws OWLServerException {
        this.transport = transport;
    }

    @Override
    public void addServerListener(ServerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeServerListener(ServerListener listener) {
        int index = listeners.indexOf(listener);
        if (index != -1) {
            listeners.remove(index);
        }
    }

    @Override
    public void start(AuthToken token) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop(AuthToken token) {
        // TODO Auto-generated method stub
        
    }

    /*
     * Deprecated classes
     */

    @Override
    @Deprecated
    public OntologyDocumentRevision evaluateRevisionPointer(org.protege.owl.server.api.AuthToken u, ServerOntologyDocument doc, RevisionPointer pointer) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public ServerDocument getServerDocument(org.protege.owl.server.api.AuthToken u, ServerPath serverIRI) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public Collection<ServerDocument> list(org.protege.owl.server.api.AuthToken u, ServerDirectory dir) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public Collection<ServerDocument> list(AuthenticationDetails token, ProjectId projectId) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public ServerDirectory createDirectory(org.protege.owl.server.api.AuthToken u, ServerPath serverIRI) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public ServerOntologyDocument createOntologyDocument(org.protege.owl.server.api.AuthToken u, ServerPath serverIRI, Map<String, Object> settings) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public void setTransports(Collection<ServerTransport> transports) {
    }

    @Override
    @Deprecated
    public Collection<ServerTransport> getTransports() {
        return null;
    }

    @Override
    @Deprecated
    public ChangeHistory getChanges(org.protege.owl.server.api.AuthToken u, ServerOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException {
        return null;
    }

    @Override
    @Deprecated
    public void commit(org.protege.owl.server.api.AuthToken u, ServerOntologyDocument doc, SingletonChangeHistory changes) throws OWLServerException {
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
    public void shutdown(org.protege.owl.server.api.AuthToken u) {
    }
}
