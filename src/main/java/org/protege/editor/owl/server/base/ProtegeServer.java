package org.protege.editor.owl.server.base;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.TransportHandler;
import org.protege.editor.owl.server.api.exception.OWLServerException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.HistoryFile;
import org.protege.editor.owl.server.versioning.InvalidHistoryFileException;
import org.protege.editor.owl.server.versioning.ServerDocument;

import java.util.List;
import java.util.Map;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationId;
import edu.stanford.protege.metaproject.api.OperationRegistry;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectRegistry;
import edu.stanford.protege.metaproject.api.Role;
import edu.stanford.protege.metaproject.api.RoleId;
import edu.stanford.protege.metaproject.api.RoleRegistry;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.UserRegistry;
import edu.stanford.protege.metaproject.api.exception.IdAlreadyInUseException;
import edu.stanford.protege.metaproject.api.exception.UnknownMetaprojectObjectIdException;

/**
 * The main server that acts as the end-point server where user requests to the server
 * get implemented.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ProtegeServer extends ServerLayer {

    private ServerConfiguration configuration;

    private UserRegistry userRegistry;
    private ProjectRegistry projectRegistry;
    private RoleRegistry roleRegistry;
    private OperationRegistry operationRegistry;

    private TransportHandler transport;

    public ProtegeServer(ServerConfiguration configuration) {
        this.configuration = configuration;
        userRegistry = configuration.getMetaproject().getUserRegistry();
        projectRegistry = configuration.getMetaproject().getProjectRegistry();
        roleRegistry = configuration.getMetaproject().getRoleRegistry();
    }

    @Override
    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void createUser(AuthToken token, User newUser) throws ServerServiceException {
        try {
            userRegistry.add(newUser);
        }
        catch (IdAlreadyInUseException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public void deleteUser(AuthToken token, UserId userId) throws ServerServiceException {
        try {
            User user = userRegistry.get(userId);
            userRegistry.remove(user);
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public void updateUser(AuthToken token, UserId userId, User updatedUser) throws ServerServiceException {
        try {
            userRegistry.update(userId, updatedUser);
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public void createProject(AuthToken token, Project newProject) throws ServerServiceException {
        try {
            projectRegistry.add(newProject);
        }
        catch (IdAlreadyInUseException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public void deleteProject(AuthToken token, ProjectId projectId) throws ServerServiceException {
        try {
            Project project = projectRegistry.get(projectId);
            projectRegistry.remove(project);
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public void updateProject(AuthToken token, ProjectId projectId, Project updatedProject) throws ServerServiceException {
        try {
            projectRegistry.update(projectId, updatedProject);
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public ServerDocument openProject(AuthToken token, ProjectId projectId) throws ServerServiceException {
        try {
            Project project = projectRegistry.get(projectId);
            return new ServerDocument(configuration.getHost(), new HistoryFile(project.getFile())); // TODO: Use factory
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException(e);
        }
        catch (InvalidHistoryFileException e) {
            throw new ServerServiceException("Internal server error", e);
        }
    }

    @Override
    public void createRole(AuthToken token, Role newRole) throws ServerServiceException {
        try {
            roleRegistry.add(newRole);
        }
        catch (IdAlreadyInUseException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public void deleteRole(AuthToken token, RoleId roleId) throws ServerServiceException {
        try {
            roleRegistry.remove(roleRegistry.get(roleId));
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException(e);
        }
        
    }

    @Override
    public void updateRole(AuthToken token, RoleId roleId, Role updatedRole) throws ServerServiceException {
        try {
            roleRegistry.update(roleId, updatedRole);
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public void createOperation(AuthToken token, Operation newOperation) throws ServerServiceException {
        try {
            operationRegistry.add(newOperation);
        }
        catch (IdAlreadyInUseException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public void deleteOperation(AuthToken token, OperationId operationId) throws ServerServiceException {
        try {
            operationRegistry.remove(operationRegistry.get(operationId));
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException(e);
        }
    }

    @Override
    public void updateOperation(AuthToken token, OperationId operationId, Operation updatedOperation)
            throws ServerServiceException {
        try {
            operationRegistry.update(operationId, updatedOperation);
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException(e);
        }
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
