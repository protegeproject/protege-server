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
    public void addUser(AuthToken token, User newUser) throws ServerRequestException {
        getDelegate().addUser(token, newUser);
    }

    @Override
    public void removeUser(AuthToken token, UserId userId) throws ServerRequestException {
        getDelegate().removeUser(token, userId);
    }

    @Override
    public void modifyUser(AuthToken token, UserId userId, User user) throws ServerRequestException {
        getDelegate().modifyUser(token, userId, user);
    }

    @Override
    public void addProject(AuthToken token, Project newProject) throws ServerRequestException {
        getDelegate().addProject(token, newProject);
    }

    @Override
    public void removeProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        getDelegate().removeProject(token, projectId);
    }

    @Override
    public void modifyProject(AuthToken token, ProjectId projectId, Project newProject) throws ServerRequestException {
        getDelegate().modifyProject(token, projectId, newProject);
    }

    @Override
    public ServerDocument openProject(AuthToken token, ProjectId projectId) throws ServerRequestException {
        return getDelegate().openProject(token, projectId);
    }

    @Override
    public void addRole(AuthToken token, Role newRole) throws ServerRequestException {
        getDelegate().addRole(token, newRole);
    }

    @Override
    public void removeRole(AuthToken token, RoleId roleId) throws ServerRequestException {
        getDelegate().removeRole(token, roleId);
    }

    @Override
    public void modifyRole(AuthToken token, RoleId roleId, Role newRole) throws ServerRequestException {
        getDelegate().modifyRole(token, roleId, newRole);
    }

    @Override
    public void addOperation(AuthToken token, Operation operation) throws ServerRequestException {
        getDelegate().addOperation(token, operation);
    }

    @Override
    public void removeOperation(AuthToken token, OperationId operationId) throws ServerRequestException {
        getDelegate().removeOperation(token, operationId);
    }

    @Override
    public void modifyOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws ServerRequestException {
        getDelegate().modifyOperation(token, operationId, newOperation);
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
    public void modifyServerConfiguration(AuthToken token, String property, String value)
            throws ServerRequestException {
        getDelegate().modifyServerConfiguration(token, property, value);
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
