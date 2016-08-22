package org.protege.editor.owl.server;

import edu.stanford.protege.metaproject.api.*;
import edu.stanford.protege.metaproject.impl.Operations;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.exception.OperationNotAllowedException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.policy.AccessControlFilter;
import org.protege.editor.owl.server.policy.CommitBundleImpl;
import org.protege.editor.owl.server.versioning.Commit;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessControlFilterTest {

    private ProtegeServer baseServer;
    private AccessControlFilter policyFilter;

    @Mock private Project projectX;

    @Mock private User userA;
    @Mock private User userB;

    @Mock private AuthToken tokenUserA;
    @Mock private AuthToken tokenUserB;

    @Mock private OWLOntology ontology;
    @Mock private OWLAxiom axiom1;
    @Mock private OWLAxiom axiom2;
    @Mock private OWLAxiom axiom3;
    @Mock private OWLAnnotation annotation1;
    @Mock private OWLAnnotation annotation2;
    @Mock private OWLImportsDeclaration importDecl1;
    @Mock private OWLImportsDeclaration importDecl2;
    private IRI otherOntologyId = IRI.create("http://protege.stanford.edu/ont/other-ontology-id");

    @Mock private UserId userIdA;
    @Mock private UserId userIdB;

    @Mock private Name userNameA;
    @Mock private Name userNameB;

    @Mock private ProjectId projectId;

    private DocumentRevision headRevision = DocumentRevision.START_REVISION;
    
    @Mock private ServerConfiguration configuration;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        when(tokenUserA.getUser()).thenReturn(userA);
        when(tokenUserB.getUser()).thenReturn(userB);
        
        when(userA.getId()).thenReturn(userIdA);
        when(userB.getId()).thenReturn(userIdB);
        
        when(userIdA.get()).thenReturn("user_a");
        when(userIdB.get()).thenReturn("user_b");
        
        when(userA.getName()).thenReturn(userNameA);
        when(userB.getName()).thenReturn(userNameB);
        
        when(userNameA.get()).thenReturn("User A");
        when(userNameB.get()).thenReturn("User B");
        
        when(projectX.getId()).thenReturn(projectId);
        
        when(ontology.getOntologyID()).thenReturn(new OWLOntologyID());
        
        when(configuration.isOperationAllowed(Operations.ADD_AXIOM.getId(), projectId, userIdA)).thenReturn(true);
        when(configuration.isOperationAllowed(Operations.ADD_AXIOM.getId(), projectId, userIdB)).thenReturn(true);
        
        when(configuration.isOperationAllowed(Operations.REMOVE_AXIOM.getId(), projectId, userIdA)).thenReturn(true);
        when(configuration.isOperationAllowed(Operations.REMOVE_AXIOM.getId(), projectId, userIdB)).thenReturn(false);
        
        when(configuration.isOperationAllowed(Operations.ADD_ONTOLOGY_ANNOTATION.getId(), projectId, userIdA)).thenReturn(true);
        when(configuration.isOperationAllowed(Operations.ADD_ONTOLOGY_ANNOTATION.getId(), projectId, userIdB)).thenReturn(true);
        
        when(configuration.isOperationAllowed(Operations.REMOVE_ONTOLOGY_ANNOTATION.getId(), projectId, userIdA)).thenReturn(true);
        when(configuration.isOperationAllowed(Operations.REMOVE_ONTOLOGY_ANNOTATION.getId(), projectId, userIdB)).thenReturn(false);
        
        when(configuration.isOperationAllowed(Operations.ADD_IMPORT.getId(), projectId, userIdA)).thenReturn(false);
        when(configuration.isOperationAllowed(Operations.ADD_IMPORT.getId(), projectId, userIdB)).thenReturn(true);
        
        when(configuration.isOperationAllowed(Operations.REMOVE_IMPORT.getId(), projectId, userIdA)).thenReturn(false);
        when(configuration.isOperationAllowed(Operations.REMOVE_IMPORT.getId(), projectId, userIdB)).thenReturn(true);
        
        when(configuration.isOperationAllowed(Operations.MODIFY_ONTOLOGY_IRI.getId(), projectId, userIdA)).thenReturn(true);
        when(configuration.isOperationAllowed(Operations.MODIFY_ONTOLOGY_IRI.getId(), projectId, userIdB)).thenReturn(false);
        
        System.setProperty(HTTPServer.SERVER_CONFIGURATION_PROPERTY, "server-configuration.json");
        
        baseServer = new ProtegeServer(configuration);
        policyFilter = new AccessControlFilter(baseServer);
    }

    @Test
    public void authorizeCommitTest() throws Exception {
        List<OWLOntologyChange> changes = new ArrayList<>();
        RevisionMetadata metadata = new RevisionMetadata("user_a", "User A", "user_a@example.com", "Comment");
        changes.add(new AddAxiom(ontology, axiom2));
        changes.add(new AddAxiom(ontology, axiom3));
        changes.add(new RemoveAxiom(ontology, axiom1));
        CommitBundle commitBundle = new CommitBundleImpl(headRevision, new Commit(metadata, changes));
        policyFilter.commit(tokenUserA, projectId, commitBundle);
    }

    @Test
    public void unauthorizeCommitTest() throws Exception {
        List<OWLOntologyChange> changes = new ArrayList<>();
        RevisionMetadata metadata = new RevisionMetadata("user_b", "User B", "user_b@example.com", "Comment");
        changes.add(new AddAxiom(ontology, axiom2));
        changes.add(new AddAxiom(ontology, axiom3));
        changes.add(new RemoveAxiom(ontology, axiom1));
        CommitBundle commitBundle = new CommitBundleImpl(headRevision, new Commit(metadata, changes));
        
        thrown.expect(ServerServiceException.class);
        thrown.expectCause(new CauseMatcher(OperationNotAllowedException.class,
                "User has no permission for 'Remove axiom' operation"));
        
        policyFilter.commit(tokenUserB, projectId, commitBundle);
    }

    @Test
    public void unauthorizeOperationsUserATest() throws Exception {
        List<OWLOntologyChange> changes = new ArrayList<>();
        RevisionMetadata metadata = new RevisionMetadata("user_a", "User A", "user_a@example.com", "Comment");
        changes.add(new AddAxiom(ontology, axiom2));
        changes.add(new RemoveAxiom(ontology, axiom1));
        changes.add(new AddOntologyAnnotation(ontology, annotation2));
        changes.add(new RemoveOntologyAnnotation(ontology, annotation1));
        changes.add(new AddImport(ontology, importDecl2));
        changes.add(new RemoveImport(ontology, importDecl1));
        changes.add(new SetOntologyID(ontology, otherOntologyId));
        CommitBundle commitBundle = new CommitBundleImpl(headRevision, new Commit(metadata, changes));
        
        thrown.expect(ServerServiceException.class);
        // Operations are stored in Sets, which makes these exceptions have random order
        //thrown.expectCause(new CauseMatcher(OperationNotAllowedException.class,
               //  "User has no permission for 'Remove ontology import', 'Add ontology import' operations"));
        
        policyFilter.commit(tokenUserA, projectId, commitBundle);
    }

    @Test
    public void unauthorizeOperationsUserBTest() throws Exception {
        List<OWLOntologyChange> changes = new ArrayList<>();
        RevisionMetadata metadata = new RevisionMetadata("user_b", "User B", "user_b@example.com", "Comment");
        changes.add(new AddAxiom(ontology, axiom2));
        changes.add(new RemoveAxiom(ontology, axiom1));
        changes.add(new AddOntologyAnnotation(ontology, annotation2));
        changes.add(new RemoveOntologyAnnotation(ontology, annotation1));
        changes.add(new AddImport(ontology, importDecl2));
        changes.add(new RemoveImport(ontology, importDecl1));
        changes.add(new SetOntologyID(ontology, otherOntologyId));
        CommitBundle commitBundle = new CommitBundleImpl(headRevision, new Commit(metadata, changes));
        
        thrown.expect(ServerServiceException.class);
        // Operations are stored in Sets, which makes these exceptions have random order
        //thrown.expectCause(new CauseMatcher(OperationNotAllowedException.class,
                //"User has no permission for 'Remove axiom', 'Remove ontology annotation', 'Modify the ontology IRI' operations"));
        
        policyFilter.commit(tokenUserB, projectId, commitBundle);
    }
}
