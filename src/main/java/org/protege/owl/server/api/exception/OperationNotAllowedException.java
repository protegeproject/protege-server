package org.protege.owl.server.api.exception;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.protege.metaproject.api.Operation;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class OperationNotAllowedException extends Exception
{
    private static final long serialVersionUID = 4743099967379984009L;

    private Set<Operation> operations = new HashSet<>();

    public OperationNotAllowedException(Operation operation) {
        operations.add(operation);
    }

    public OperationNotAllowedException(Set<Operation> operations) {
        this.operations.addAll(operations);
    }

    public static OperationNotAllowedException create(List<Exception> exceptions) {
        Set<Operation> violatedOperations = new HashSet<>();
        for (Exception e : exceptions) {
            if (e instanceof OperationNotAllowedException) {
                violatedOperations.addAll(((OperationNotAllowedException) e).getOperations());
            }
        }
        return new OperationNotAllowedException(violatedOperations);
    }

    public Set<Operation> getOperations() {
        return operations;
    }

    @Override
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        boolean needComma = false;
        for (Operation op : operations) {
            if (needComma) {
                sb.append(", ");
            }
            sb.append(op.getName());
            needComma = true;
        }
        return String.format("User has no permission for %s operation", sb.toString());
    }
}
