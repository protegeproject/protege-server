package org.protege.owl.server.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerTransport;
import org.protege.owl.server.api.User;
import org.protege.owl.server.api.exception.ServerException;
import org.protege.owl.server.api.exception.UserNotAuthenticated;
import org.protege.owl.server.connect.rmi.RMITransport;
import org.protege.owl.server.policy.generated.UsersAndGroupsLexer;
import org.protege.owl.server.policy.generated.UsersAndGroupsParser;
import org.semanticweb.owlapi.model.IRI;

public class Authenticator extends ServerFilter {
    private Logger logger = Logger.getLogger(Authenticator.class.getCanonicalName());
    private UserDatabase userDb;
    private BasicLoginService loginService;

    public Authenticator(Server delegate) throws IOException, RecognitionException, ServerException {
        super(delegate);
        parse();
        loginService = new BasicLoginService(userDb);
    }
    
    private void parse() throws IOException, RecognitionException, ServerException {
        File usersAndGroups = getConfiguration("UsersAndGroups");
        FileInputStream fis = new FileInputStream(usersAndGroups);
        try {
            ANTLRInputStream input = new ANTLRInputStream(fis);
            UsersAndGroupsLexer lexer = new UsersAndGroupsLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            UsersAndGroupsParser parser = new UsersAndGroupsParser(tokens);
            parser.top();
            userDb = parser.getUserDatabase();
        }
        finally {
            fis.close();
        }
    }

    public void setTransports(Collection<ServerTransport> transports) {
        try {
            loadRMITransports(transports);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Authentication Service failed to start up.  Users may not be able to authenticate.", e);
        }
        getDelegate().setTransports(transports);
    }
    
    private void loadRMITransports(Collection<ServerTransport> transports) throws RemoteException, AlreadyBoundException {
        int rmitransports = 0;
        int othertransports = 0;
        for (ServerTransport transport : transports) {
            if (transport instanceof RMITransport) {
                rmitransports++;
                loadRMITransport((RMITransport) transport);
            }
            else {
                othertransports++;
            }
        }
        if (rmitransports == 0) {
            logger.warning("Did not find communications suitable for login.  Clients will be locked out.");
        }
        if (othertransports > 0) {
            logger.warning("Clients must use RMI to login.  Thereafter they can communicate with the server in other ways.");
        }      
    }
    
    private void loadRMITransport(RMITransport transport) throws RemoteException, AlreadyBoundException {
        int serverPort = transport.getServerPort();
        Remote remote = UnicastRemoteObject.exportObject(loginService, serverPort);
        transport.getRegistry().bind(LoginService.SERVICE, remote);
        logger.info("Authentication service started");
    }
    
    private void ensureUserIdCorrect(User u) throws UserNotAuthenticated {
        if (!loginService.checkAuthentication(u)) {
            throw new UserNotAuthenticated();
        }
    }

    public Collection<ServerTransport> getTransports() {
        return getDelegate().getTransports();
    }

    public ServerDocument getServerDocument(User u, IRI serverIRI) throws ServerException  {
        ensureUserIdCorrect(u);
        return getDelegate().getServerDocument(u, serverIRI);
    }

    public Collection<ServerDocument> list(User u, ServerDirectory dir) throws ServerException {
        ensureUserIdCorrect(u);
        return getDelegate().list(u, dir);
    }

    public ServerDirectory createDirectory(User u, IRI serverIRI) throws ServerException {
        ensureUserIdCorrect(u);
        return getDelegate().createDirectory(u, serverIRI);
    }

    public RemoteOntologyDocument createOntologyDocument(User u, IRI serverIRI, Map<String, Object> settings) throws ServerException {
        ensureUserIdCorrect(u);
        return getDelegate().createOntologyDocument(u, serverIRI, settings);
    }

    public ChangeDocument getChanges(User u, RemoteOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws ServerException {
        ensureUserIdCorrect(u);
        return getDelegate().getChanges(u, doc, start, end);
    }

    public ChangeDocument commit(User u, RemoteOntologyDocument doc, ChangeMetaData commitComment, ChangeDocument changes, SortedSet<OntologyDocumentRevision> myCommits) throws ServerException {
        ensureUserIdCorrect(u);
        ChangeDocument serverSideChanges = getChanges(u, doc, OntologyDocumentRevision.START_REVISION, null);
        Map<OntologyDocumentRevision, ChangeMetaData> metaDataMap = serverSideChanges.getMetaData();
        for (OntologyDocumentRevision revision : myCommits) {
            ChangeMetaData metaData = metaDataMap.get(revision);
            if (metaData == null) {
                throw new IllegalStateException("No commit comment, etc found for previous commit");
            }
            if (!u.getUserName().equals(metaData.getUsername())) {
                throw new IllegalStateException("Caller is claiming a commit he did not do.  Is someone trying to pull something?");
            }
        }
        if (commitComment == null) {
            commitComment = new ChangeMetaData();
        }
        commitComment.setUser(u);
        return getDelegate().commit(u, doc, commitComment, changes, myCommits);
    }

    public void shutdown() {
        getDelegate().shutdown();
    }

    public File getConfiguration(String fileName) throws ServerException {
        return getDelegate().getConfiguration(fileName);
    }

    public File getConfiguration(ServerDocument doc, String extension) throws ServerException {
        return getDelegate().getConfiguration(doc, extension);
    } 

}
