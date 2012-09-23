package org.protege.owl.server.policy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RevisionPointer;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.ServerOntologyDocument;
import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.api.SingletonChangeHistory;
import org.protege.owl.server.api.exception.AuthorizationFailedException;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.policy.Authenticator.GetUserAndGroupOption;
import org.protege.owl.server.policy.generated.PolicyLexer;
import org.protege.owl.server.policy.generated.PolicyParser;
import org.protege.owl.server.util.ServerFilterAdapter;

public class PolicyFilter extends ServerFilterAdapter {
    private UserDatabase userDb;
    private Policy policy;
    
    public static Policy parsePolicy(Server server) throws IOException, RecognitionException, OWLServerException {
        InputStream fis = server.getConfigurationInputStream("Policy");
        try {
            ANTLRInputStream input = new ANTLRInputStream(fis);
            PolicyLexer lexer = new PolicyLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PolicyParser parser = new PolicyParser(tokens);
            parser.top();
            return parser.getPolicy();
        }
        finally {
            fis.close();
        }
    }
    
    public PolicyFilter(Server delegate) throws IOException, RecognitionException, OWLServerException {
        super(delegate);
        policy = parsePolicy(delegate);
        userDb = Authenticator.parseUsersAndGroups(delegate, GetUserAndGroupOption.USE_CACHE);
    }
    
    @Override
    public OntologyDocumentRevision evaluateRevisionPointer(AuthToken u, ServerOntologyDocument doc, RevisionPointer pointer) throws OWLServerException {
        if (!policy.checkPermission(userDb, u.getUserId(), doc.getServerPath(), Operation.READ)) {
            throw new AuthorizationFailedException("Attempted read operation not allowed");
        }
        return super.evaluateRevisionPointer(u, doc, pointer);
    }
    
    @Override
    public Collection<ServerDocument> list(AuthToken u, ServerDirectory dir) throws OWLServerException {
        if (!policy.checkPermission(userDb, u.getUserId(), dir.getServerPath(), Operation.READ)) {
            throw new AuthorizationFailedException("Attempted read on directory not allowed");
        }
        return super.list(u, dir);
    }
    
    @Override
    public ServerDirectory createDirectory(AuthToken u, ServerPath serverPath) throws OWLServerException {
        if (!serverPath.isRoot() &&
                !policy.checkPermission(userDb, u.getUserId(), serverPath.getParent(), Operation.WRITE)) {
            throw new AuthorizationFailedException("Attempted create not allowed");
        }
        return super.createDirectory(u, serverPath);
    }
    
    @Override
    public ServerOntologyDocument createOntologyDocument(AuthToken u, ServerPath serverPath, Map<String, Object> settings) throws OWLServerException {
        if (!serverPath.isRoot() &&
                !policy.checkPermission(userDb, u.getUserId(), serverPath.getParent(), Operation.WRITE)) {
            throw new AuthorizationFailedException("Attempted create not allowed");
        }
        return super.createOntologyDocument(u, serverPath, settings);
    }

    @Override
    public ChangeHistory getChanges(AuthToken u, ServerOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException {
        if (!policy.checkPermission(userDb, u.getUserId(), doc.getServerPath(), Operation.READ)) {
            throw new AuthorizationFailedException("Attempted read not allowed");
        }
        return super.getChanges(u, doc, start, end);
    }
    
    @Override
    public void commit(AuthToken u, ServerOntologyDocument doc, SingletonChangeHistory changes) throws OWLServerException {
        if (!policy.checkPermission(userDb, u.getUserId(), doc.getServerPath(), Operation.WRITE)) {
            throw new AuthorizationFailedException("Attempted write not allowed");
        }
        super.commit(u, doc, changes);
    }
    
}
