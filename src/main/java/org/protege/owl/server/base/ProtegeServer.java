package org.protege.owl.server.base;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.ServerLayer;
import org.protege.owl.server.api.TransportHandler;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.exception.ServerServiceException;
import org.protege.owl.server.versioning.ServerDocument;

import java.util.List;
import java.util.Map;

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

    public ProtegeServer(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void createUser(AuthToken token, User newUser) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteUser(AuthToken token, UserId userId) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateUser(AuthToken token, UserId userId, User user) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createProject(AuthToken token, Project newProject) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteProject(AuthToken token, ProjectId projectId) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateProject(AuthToken token, ProjectId projectId, Project newProject) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ServerDocument openProject(AuthToken token, ProjectId projectId) throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createRole(AuthToken token, Role newRole) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteRole(AuthToken token, RoleId roleId) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateRole(AuthToken token, RoleId roleId, Role newRole) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createOperation(AuthToken token, Operation operation) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteOperation(AuthToken token, OperationId operationId) throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws ServerServiceException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setServerConfiguration(AuthToken token, String property, String value)
            throws ServerServiceException {
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
    public List<User> getAllUsers(AuthToken token) throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Project> getProjects(AuthToken token, UserId userId) throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Project> getAllProjects(AuthToken token) throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId) throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId) throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Role> getAllRoles(AuthToken token) throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId) throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId)
            throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Operation> getAllOperations(AuthToken token) throws ServerServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId)
            throws ServerServiceException {
        // TODO Auto-generated method stub
        return false;
    }
}
