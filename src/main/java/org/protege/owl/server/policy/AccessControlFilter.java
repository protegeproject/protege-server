package org.protege.owl.server.policy;

import org.protege.owl.server.api.CommitBundle;
import org.protege.owl.server.api.PerOperationCommitBundle;
import org.protege.owl.server.api.ServerFilterAdapter;
import org.protege.owl.server.api.ServerLayer;
import org.protege.owl.server.api.exception.OperationNotAllowedException;
import org.protege.owl.server.api.exception.ServerRequestException;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Metaproject;
import edu.stanford.protege.metaproject.api.Operation;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.MetaprojectException;
import edu.stanford.protege.metaproject.impl.Operations;

/**
 * Represents the access control gate that will check each user request to their given permission.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AccessControlFilter extends ServerFilterAdapter {

    private final Metaproject metaproject;

    public AccessControlFilter(ServerLayer delegate) {
        super(delegate);
        metaproject = getConfiguration().getMetaproject();
    }

    @Override
    public void commit(AuthToken token, Project project, CommitBundle commitBundle) throws ServerRequestException {
        if (commitBundle instanceof PerOperationCommitBundle) {
            evaluatePerOperationCommitBundle(token, project, (PerOperationCommitBundle) commitBundle);
        }
        else {
            evaluateCommitBundle(token, project, commitBundle);
        }
    }

    private void evaluatePerOperationCommitBundle(AuthToken token, Project project, PerOperationCommitBundle commitBundle)
            throws ServerRequestException {
        try {
            Operation operation = commitBundle.getOperation();
            if (checkPermission(token.getUserId(), project.getId(), operation)) {
                getDelegate().commit(token, project, commitBundle);
            }
            else {
                throw new ServerRequestException(new OperationNotAllowedException(operation));
            }
        }
        catch (MetaprojectException e) {
            throw new ServerRequestException(e);
        }
    }

    private void evaluateCommitBundle(AuthToken token, Project project, CommitBundle commitBundle)
            throws ServerRequestException {
        List<Operation> operations = evaluateCommitChanges(commitBundle);
        List<Exception> violations = new ArrayList<>();
        if (checkPermission(token.getUserId(), project.getId(), operations, violations)) {
            getDelegate().commit(token, project, commitBundle);
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
        if (!metaproject.isOperationAllowed(operation.getId(), projectId, userId)) {
            return false;
        }
        return true;
    }

    private List<Operation> evaluateCommitChanges(CommitBundle commitBundle) throws ServerRequestException {
        final List<OWLOntologyChange> changes = commitBundle.getChanges();
        List<Operation> operations = new ArrayList<>();
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
        String template = "No suitable operation for ontology change %s";
        throw new OperationForChangeNotFoundException(String.format(template, change.toString()));
    }
}
