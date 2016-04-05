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
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationId;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.Role;
import edu.stanford.protege.metaproject.api.RoleId;
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
public class ProtegeServer extends ServerLayer {

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
    public void modifyUser(AuthToken token, UserId userId, User user) throws ServerRequestException {
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
    public void modifyProject(AuthToken token, ProjectId projectId, Project newProject) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void viewProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addRole(AuthToken token, Role newRole) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeRole(AuthToken token, RoleId roleId) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void modifyRole(AuthToken token, RoleId roleId, Role newRole) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addOperation(AuthToken token, Operation operation) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeOperation(AuthToken token, OperationId operationId) throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void modifyOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void modifyServerConfiguration(AuthToken token, String property, String value)
            throws ServerRequestException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle changes) {
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
}
