package org.protege.owl.server.core;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.ServerLayer;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.api.server.ServerListener;
import org.protege.owl.server.api.server.TransportHandler;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.protege.metaproject.api.AuthToken;
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
public class ProtegeServer implements ServerLayer {

    private ServerConfiguration configuration;

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
        try {
            transport.bind(this);
        }
        catch (Exception e) {
            throw new OWLServerException(e);
        }
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
}
