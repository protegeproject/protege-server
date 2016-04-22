package org.protege.editor.owl.server.base;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.TransportHandler;
import org.protege.editor.owl.server.api.exception.OWLServerException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.HistoryFile;
import org.protege.editor.owl.server.versioning.InvalidHistoryFileException;
import org.protege.editor.owl.server.versioning.ServerDocument;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.stanford.protege.metaproject.Manager;
import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Host;
import edu.stanford.protege.metaproject.api.MetaprojectAgent;
import edu.stanford.protege.metaproject.api.MetaprojectFactory;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationId;
import edu.stanford.protege.metaproject.api.OperationRegistry;
import edu.stanford.protege.metaproject.api.Policy;
import edu.stanford.protege.metaproject.api.Port;
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
    private Policy policy;
    private MetaprojectAgent metaprojectAgent;

    private TransportHandler transport;

    private static final MetaprojectFactory metaprojectFactory = Manager.getFactory();

    public ProtegeServer(ServerConfiguration configuration) {
        this.configuration = configuration;
        userRegistry = configuration.getMetaproject().getUserRegistry();
        projectRegistry = configuration.getMetaproject().getProjectRegistry();
        roleRegistry = configuration.getMetaproject().getRoleRegistry();
        policy = configuration.getMetaproject().getPolicy();
        metaprojectAgent = configuration.getMetaproject().getMetaprojectAgent();
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
        policy.add(roleId, projectId, userId);
    }

    @Override
    public void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws ServerServiceException {
        policy.remove(userId, projectId, roleId);
    }

    @Override
    public Host getHost() throws Exception {
        return configuration.getHost();
    }

    @Override
    public void setHostAddress(AuthToken token, String hostAddress) throws Exception {
        URI hostAddresssUri = metaprojectFactory.getUri(hostAddress);
        Optional<Port> secondaryPort = getHost().getSecondaryPort();
        Host updatedHost = metaprojectFactory.getHost(hostAddresssUri, secondaryPort);
        configuration.setHost(updatedHost);
    }

    @Override
    public void setSecondaryPort(AuthToken token, int portNumber) throws Exception {
        URI hostAddress = getHost().getUri();
        Optional<Port> secondaryPort = Optional.empty();
        if (portNumber > 0) {
            secondaryPort = Optional.of(metaprojectFactory.getPort(portNumber));
        }
        Host updatedHost = metaprojectFactory.getHost(hostAddress, secondaryPort);
        configuration.setHost(updatedHost);
    }

    @Override
    public String getRootDirectory() throws Exception {
        return configuration.getServerRoot().toString();
    }

    @Override
    public void setRootDirectory(AuthToken token, String rootDirectory) throws Exception {
        configuration.setServerRoot(new File(rootDirectory));
    }

    @Override
    public Map<String, String> getServerProperties() throws Exception {
        return configuration.getProperties();
    }

    @Override
    public void setServerConfiguration(AuthToken token, String property, String value)
            throws ServerServiceException {
        configuration.addProperty(property, value);
    }

    @Override
    public void unsetServerConfiguration(AuthToken token, String property) throws Exception {
        configuration.removeProperty(property);
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
        return new ArrayList<>(userRegistry.getEntries());
    }

    @Override
    public List<Project> getProjects(AuthToken token, UserId userId) throws ServerServiceException {
        return new ArrayList<>(metaprojectAgent.getProjects(userId));
    }

    @Override
    public List<Project> getAllProjects(AuthToken token) throws ServerServiceException {
        return new ArrayList<>(projectRegistry.getEntries());
    }

    @Override
    public Map<ProjectId, List<Role>> getRoles(AuthToken token, UserId userId) throws ServerServiceException {
        Map<ProjectId, List<Role>> roleMap = new HashMap<>();
        for (Project project : getAllProjects(token)) {
            roleMap.put(project.getId(), getRoles(token, userId, project.getId()));
        }
        return roleMap;
    }

    @Override
    public List<Role> getRoles(AuthToken token, UserId userId, ProjectId projectId) throws ServerServiceException {
        return new ArrayList<>(metaprojectAgent.getRoles(userId, projectId));
    }

    @Override
    public List<Role> getAllRoles(AuthToken token) throws ServerServiceException {
        return new ArrayList<>(roleRegistry.getEntries());
    }

    @Override
    public Map<ProjectId, List<Operation>> getOperations(AuthToken token, UserId userId) throws ServerServiceException {
        Map<ProjectId, List<Operation>> operationMap = new HashMap<>();
        for (Project project : getAllProjects(token)) {
            operationMap.put(project.getId(), getOperations(token, userId, project.getId()));
        }
        return operationMap;
    }

    @Override
    public List<Operation> getOperations(AuthToken token, UserId userId, ProjectId projectId)
            throws ServerServiceException {
        return new ArrayList<>(metaprojectAgent.getOperations(userId, projectId));
    }

    @Override
    public List<Operation> getAllOperations(AuthToken token) throws ServerServiceException {
        return new ArrayList<>(operationRegistry.getEntries());
    }

    @Override
    public boolean isOperationAllowed(AuthToken token, OperationId operationId, ProjectId projectId, UserId userId)
            throws ServerServiceException {
        return metaprojectAgent.isOperationAllowed(operationId, projectId, userId);
    }
}
