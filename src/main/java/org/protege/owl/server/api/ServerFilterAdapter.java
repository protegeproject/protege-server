package org.protege.owl.server.api;

import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.api.server.ServerListener;
import org.protege.owl.server.api.server.TransportHandler;
import org.protege.owl.server.changes.ServerDocument;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationId;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.Role;
import edu.stanford.protege.metaproject.api.RoleId;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ServerFilterAdapter extends AbstractServerFilter {

    public ServerFilterAdapter(ServerLayer delegate) {
        super(delegate);
    }

    @Override
    public void createUser(AuthToken token, User newUser) throws ServerRequestException {
        getDelegate().createUser(token, newUser);
    }

    @Override
    public void deleteUser(AuthToken token, UserId userId) throws ServerRequestException {
        getDelegate().deleteUser(token, userId);
    }

    @Override
    public void updateUser(AuthToken token, UserId userId, User user) throws ServerRequestException {
        getDelegate().updateUser(token, userId, user);
    }

    @Override
    public void createProject(AuthToken token, Project newProject) throws ServerRequestException {
        getDelegate().createProject(token, newProject);
    }

    @Override
    public void deleteProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        getDelegate().deleteProject(token, projectId);
    }

    @Override
    public void updateProject(AuthToken token, ProjectId projectId, Project newProject) throws ServerRequestException {
        getDelegate().updateProject(token, projectId, newProject);
    }

    @Override
    public ServerDocument openProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        return getDelegate().openProject(token, projectId);
    }

    @Override
    public void createRole(AuthToken token, Role newRole) throws ServerRequestException {
        getDelegate().createRole(token, newRole);
    }

    @Override
    public void deleteRole(AuthToken token, RoleId roleId) throws ServerRequestException {
        getDelegate().deleteRole(token, roleId);
    }

    @Override
    public void updateRole(AuthToken token, RoleId roleId, Role newRole) throws ServerRequestException {
        getDelegate().updateRole(token, roleId, newRole);
    }

    @Override
    public void createOperation(AuthToken token, Operation operation) throws ServerRequestException {
        getDelegate().createOperation(token, operation);
    }

    @Override
    public void deleteOperation(AuthToken token, OperationId operationId) throws ServerRequestException {
        getDelegate().deleteOperation(token, operationId);
    }

    @Override
    public void updateOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws ServerRequestException {
        getDelegate().updateOperation(token, operationId, newOperation);
    }

    @Override
    public void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws ServerRequestException {
        getDelegate().assignRole(token, userId, projectId, roleId);
    }

    @Override
    public void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws ServerRequestException {
        getDelegate().retractRole(token, userId, projectId, roleId);
    }

    @Override
    public void updateServerConfiguration(AuthToken token, String property, String value)
            throws ServerRequestException {
        getDelegate().updateServerConfiguration(token, property, value);
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle changes) throws ServerRequestException {
        getDelegate().commit(token, project, changes);
    }

    @Override
    public void setTransport(TransportHandler transport) throws OWLServerException {
        getDelegate().setTransport(transport);
    }

    @Override
    public void addServerListener(ServerListener listener) {
        getDelegate().addServerListener(listener);
    }

    @Override
    public void removeServerListener(ServerListener listener) {
        getDelegate().removeServerListener(listener);
    }
}
