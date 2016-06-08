package org.protege.editor.owl.server.policy;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Description;
import edu.stanford.protege.metaproject.api.MetaprojectAgent;
import edu.stanford.protege.metaproject.api.Name;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationId;
import edu.stanford.protege.metaproject.api.Password;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectOptions;
import edu.stanford.protege.metaproject.api.Role;
import edu.stanford.protege.metaproject.api.RoleId;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.impl.Operations;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.PerOperationCommitBundle;
import org.protege.editor.owl.server.api.ServerFilterAdapter;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OperationNotAllowedException;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.versioning.Commit;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.ServerDocument;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represents the access control gate that will check each user request to their given permission.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AccessControlFilter extends ServerFilterAdapter {

    private Logger logger = LoggerFactory.getLogger(AccessControlFilter.class);

    private final MetaprojectAgent metaprojectAgent;

    public AccessControlFilter(ServerLayer delegate) {
        super(delegate);
        metaprojectAgent = getConfiguration().getMetaproject().getMetaprojectAgent();
    }

    protected void checkPermission(User user, Operation operation) throws ServerServiceException {
        if (!metaprojectAgent.isOperationAllowed(operation.getId(), user.getId())) {
            OperationNotAllowedException e = new OperationNotAllowedException(operation);
            logger.error(printLog(user, operation.getName().get(), "Request rejected. Operation is not allowed"));
            throw new ServerServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void createUser(AuthToken token, User newUser, Optional<? extends Password> password)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.ADD_USER);
        super.createUser(token, newUser, password);
    }

    @Override
    public void deleteUser(AuthToken token, UserId userId) throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.REMOVE_USER);
        super.deleteUser(token, userId);
    }

    @Override
    public void updateUser(AuthToken token, UserId userId, User user, Optional<? extends Password> password)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.MODIFY_USER);
        super.updateUser(token, userId, user, password);
    }

    @Override
    public ServerDocument createProject(AuthToken token, ProjectId projectId, Name projectName, Description description,
            UserId owner, Optional<ProjectOptions> options) throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.ADD_PROJECT);
        return super.createProject(token, projectId, projectName, description, owner, options);
    }

    @Override
    public void deleteProject(AuthToken token, ProjectId projectId, boolean includeFile)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.REMOVE_PROJECT);
        super.deleteProject(token, projectId, includeFile);
    }

    @Override
    public void updateProject(AuthToken token, ProjectId projectId, Project newProject)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.MODIFY_PROJECT);
        super.updateProject(token, projectId, newProject);
    }

    @Override
    public ServerDocument openProject(AuthToken token, ProjectId projectId)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.OPEN_PROJECT);
        return super.openProject(token, projectId);
    }

    @Override
    public void createRole(AuthToken token, Role newRole) throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.ADD_ROLE);
        super.createRole(token, newRole);
    }

    @Override
    public void deleteRole(AuthToken token, RoleId roleId) throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.REMOVE_ROLE);
        super.deleteRole(token, roleId);
    }

    @Override
    public void updateRole(AuthToken token, RoleId roleId, Role newRole)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.MODIFY_ROLE);
        super.updateRole(token, roleId, newRole);
    }

    @Override
    public void createOperation(AuthToken token, Operation operation)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.ADD_OPERATION);
        super.createOperation(token, operation);
    }

    @Override
    public void deleteOperation(AuthToken token, OperationId operationId)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.REMOVE_OPERATION);
        super.deleteOperation(token, operationId);
    }

    @Override
    public void updateOperation(AuthToken token, OperationId operationId, Operation newOperation)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.MODIFY_OPERATION);
        super.updateOperation(token, operationId, newOperation);
    }

    @Override
    public void assignRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.ASSIGN_ROLE);
        super.assignRole(token, userId, projectId, roleId);
    }

    @Override
    public void retractRole(AuthToken token, UserId userId, ProjectId projectId, RoleId roleId)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.RETRACT_ROLE);
        super.retractRole(token, userId, projectId, roleId);
    }

    @Override
    public void setHostAddress(AuthToken token, URI hostAddress) throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.MODIFY_SERVER_SETTINGS);
        super.setHostAddress(token, hostAddress);
    }

    @Override
    public void setSecondaryPort(AuthToken token, int portNumber)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.MODIFY_SERVER_SETTINGS);
        super.setSecondaryPort(token, portNumber);
    }

    @Override
    public void setRootDirectory(AuthToken token, String rootDirectory)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.MODIFY_SERVER_SETTINGS);
        super.setRootDirectory(token, rootDirectory);
    }

    @Override
    public void setServerProperty(AuthToken token, String property, String value)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.MODIFY_SERVER_SETTINGS);
        super.setServerProperty(token, property, value);
    }

    @Override
    public void unsetServerProperty(AuthToken token, String property)
            throws AuthorizationException, ServerServiceException {
        checkPermission(token.getUser(), Operations.MODIFY_SERVER_SETTINGS);
        super.unsetServerProperty(token, property);
    }

    @Override
    public ChangeHistory commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException {
        if (commitBundle instanceof PerOperationCommitBundle) {
            return evaluatePerOperationCommitBundle(token, projectId, (PerOperationCommitBundle) commitBundle);
        }
        else {
            return evaluateCommitBundle(token, projectId, commitBundle);
        }
    }

    private ChangeHistory evaluatePerOperationCommitBundle(AuthToken token, ProjectId projectId, PerOperationCommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException {
        User user = token.getUser();
        Operation operation = commitBundle.getOperation();
        if (metaprojectAgent.isOperationAllowed(operation.getId(), projectId, user.getId())) {
            return getDelegate().commit(token, projectId, commitBundle);
        }
        else {
            OperationNotAllowedException e = new OperationNotAllowedException(operation);
            logger.error(printLog(user, operation.getName().get(), "Request rejected. Operation is not allowed"));
            throw new ServerServiceException(e.getMessage(), e);
        }
    }

    private ChangeHistory evaluateCommitBundle(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws AuthorizationException, OutOfSyncException, ServerServiceException {
        User user = token.getUser();
        Set<Operation> operations = evaluateCommitChanges(commitBundle);
        List<Exception> violations = new ArrayList<>();
        batchCheckPermission(user.getId(), projectId, operations, violations);
        if (violations.isEmpty()) {
            return getDelegate().commit(token, projectId, commitBundle);
        }
        else {
            OperationNotAllowedException e = OperationNotAllowedException.create(violations);
            logger.error(printLog(user, printSet(operations), "Request rejected. Operation is not allowed"));
            throw new ServerServiceException(e.getMessage(), e);
        }
    }

    private void batchCheckPermission(UserId userId, ProjectId projectId, Set<Operation> operations, List<Exception> violations) {
        for (Operation operation : operations) {
            if (!metaprojectAgent.isOperationAllowed(operation.getId(), projectId, userId)) {
                Exception e = new OperationNotAllowedException(operation);
                violations.add(e);
            }
        }
    }

    private Set<Operation> evaluateCommitChanges(CommitBundle commitBundle) throws ServerServiceException {
        final List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (Commit commit : commitBundle.getCommits()) {
            changes.addAll(commit.getChanges());
        }
        final Set<Operation> operations = new HashSet<>();
        for (OWLOntologyChange change : changes) {
            Operation op = getOperationForChange(change);
            operations.add(op);
        }
        return operations;
    }

    private Operation getOperationForChange(OWLOntologyChange change) throws OperationForChangeNotFoundException {
        if (change instanceof AddAxiom) {
            return Operations.ADD_AXIOM;
        }
        else if (change instanceof RemoveAxiom) {
            return Operations.REMOVE_AXIOM;
        }
        else if (change instanceof AddOntologyAnnotation) {
            return Operations.ADD_ONTOLOGY_ANNOTATION;
        }
        else if (change instanceof RemoveOntologyAnnotation) {
            return Operations.REMOVE_ONTOLOGY_ANNOTATION;
        }
        else if (change instanceof AddImport) {
            return Operations.ADD_IMPORT;
        }
        else if (change instanceof RemoveImport) {
            return Operations.REMOVE_IMPORT;
        }
        else if (change instanceof SetOntologyID) {
            return Operations.MODIFY_ONTOLOGY_IRI;
        }
        String message = String.format("No suitable operation for ontology change %s", change.toString());
        logger.error(printLog(null, "Commit changes", message));
        throw new OperationForChangeNotFoundException(message);
    }

    private static String printSet(Set<Operation> operations) {
        StringBuilder sb = new StringBuilder();
        boolean needComma = false;
        for (Operation op : operations) {
            if (needComma) {
                sb.append(", ");
            }
            sb.append(op.getName().get());
            needComma = true;
        }
        return sb.toString();
    }
}
