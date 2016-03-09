package org.protege.owl.server.policy;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.PerOperationCommitBundle;
import org.protege.owl.server.api.exception.ServerRequestException;
import org.protege.owl.server.api.exception.OperationNotAllowedException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerFilterAdapter;

import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.OperationRegistry;
import edu.stanford.protege.metaproject.api.Policy;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.MetaprojectException;
import edu.stanford.protege.metaproject.api.exception.OperationForChangeNotFoundException;

/**
 * Represents the access control gate that will check each user request to their given permission.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AccessControlFilter extends ServerFilterAdapter {

    private final OperationRegistry operationRegistry;
    private final Policy policy;

    public AccessControlFilter(Server delegate) {
        super(delegate);
        operationRegistry = getConfiguration().getMetaproject().getOperationRegistry();
        policy = getConfiguration().getMetaproject().getPolicy();
    }

    @Override
    public void commit(AuthToken token, ProjectId projectId, CommitBundle commitBundle) throws ServerRequestException {
        if (commitBundle instanceof PerOperationCommitBundle) {
            evaluatePerOperationCommitBundle(token, projectId, (PerOperationCommitBundle) commitBundle);
        }
        else {
            evaluateCommitBundle(token, projectId, commitBundle);
        }
    }

    private void evaluatePerOperationCommitBundle(AuthToken token, ProjectId projectId, PerOperationCommitBundle commitBundle)
            throws ServerRequestException {
        try {
            Operation operation = commitBundle.getOperation();
            if (checkPermission(token.getUserId(), projectId, operation)) {
                getDelegate().commit(token, projectId, commitBundle);
            }
            else {
                throw new ServerRequestException(new OperationNotAllowedException(operation));
            }
        }
        catch (MetaprojectException e) {
            throw new ServerRequestException(e);
        }
    }

    private void evaluateCommitBundle(AuthToken token, ProjectId projectId, CommitBundle commitBundle)
            throws ServerRequestException {
        List<Operation> operations = evaluateCommitChanges(commitBundle);
        List<Exception> violations = new ArrayList<>();
        if (checkPermission(token.getUserId(), projectId, operations, violations)) {
            getDelegate().commit(token, projectId, commitBundle);
        }
        throw new ServerRequestException(OperationNotAllowedException.create(violations));
    }

    private boolean checkPermission(UserId userId, ProjectId projectId, List<Operation> operations, List<Exception> violations)
            throws ServerRequestException {
        for (Operation op : operations) {
            try {
                if (!checkPermission(userId, projectId, op)) {
                    Exception e = new OperationNotAllowedException(op);
                    violations.add(e);
                }
            }
            catch (MetaprojectException e) {
                throw new ServerRequestException(e);
            }
        }
        return violations.isEmpty() ? true : false;
    }

    private boolean checkPermission(UserId userId, ProjectId projectId, Operation operation) throws MetaprojectException {
        if (!policy.isOperationAllowed(operation.getId(), projectId, userId)) {
            return false;
        }
        return true;
    }

    private List<Operation> evaluateCommitChanges(CommitBundle commitBundle) throws ServerRequestException {
        List<OWLOntologyChange> changes = commitBundle.getChanges();
        try {
            List<Operation> operations = new ArrayList<>();
            for (OWLOntologyChange change : changes) {
                Operation op = operationRegistry.getOperationForChange(change);
                operations.add(op);
            }
            return operations;
        }
        catch (OperationForChangeNotFoundException e) {
            throw new ServerRequestException(e);
        }
    }
}
