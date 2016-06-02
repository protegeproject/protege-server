package org.protege.editor.owl.server;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
import org.protege.editor.owl.server.policy.AccessControlFilter;
import org.protege.editor.owl.server.policy.CommitBundleImpl;
import org.protege.editor.owl.server.versioning.Commit;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Metaproject;
import edu.stanford.protege.metaproject.api.MetaprojectAgent;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.impl.Operations;

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

    @Mock private ProjectId projectId;

    private DocumentRevision headRevision = DocumentRevision.START_REVISION;

    @Mock private Metaproject metaproject;
    @Mock private ServerConfiguration configuration;
    @Mock private MetaprojectAgent metaprojectAgent;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        when(tokenUserA.getUser()).thenReturn(userA);
        when(tokenUserB.getUser()).thenReturn(userB);
        
        when(userA.getId()).thenReturn(userIdA);
        when(userB.getId()).thenReturn(userIdB);
        
        when(projectX.getId()).thenReturn(projectId);
        
        when(ontology.getOntologyID()).thenReturn(new OWLOntologyID());
        
        when(configuration.getMetaproject()).thenReturn(metaproject);
        when(metaproject.getMetaprojectAgent()).thenReturn(metaprojectAgent);
        
        when(metaprojectAgent.isOperationAllowed(Operations.ADD_AXIOM.getId(), projectId, userIdA)).thenReturn(true);
        when(metaprojectAgent.isOperationAllowed(Operations.ADD_AXIOM.getId(), projectId, userIdB)).thenReturn(true);
        
        when(metaprojectAgent.isOperationAllowed(Operations.REMOVE_AXIOM.getId(), projectId, userIdA)).thenReturn(true);
        when(metaprojectAgent.isOperationAllowed(Operations.REMOVE_AXIOM.getId(), projectId, userIdB)).thenReturn(false);
        
        when(metaprojectAgent.isOperationAllowed(Operations.ADD_ONTOLOGY_ANNOTATION.getId(), projectId, userIdA)).thenReturn(true);
        when(metaprojectAgent.isOperationAllowed(Operations.ADD_ONTOLOGY_ANNOTATION.getId(), projectId, userIdB)).thenReturn(true);
        
        when(metaprojectAgent.isOperationAllowed(Operations.REMOVE_ONTOLOGY_ANNOTATION.getId(), projectId, userIdA)).thenReturn(true);
        when(metaprojectAgent.isOperationAllowed(Operations.REMOVE_ONTOLOGY_ANNOTATION.getId(), projectId, userIdB)).thenReturn(false);
        
        when(metaprojectAgent.isOperationAllowed(Operations.ADD_IMPORT.getId(), projectId, userIdA)).thenReturn(false);
        when(metaprojectAgent.isOperationAllowed(Operations.ADD_IMPORT.getId(), projectId, userIdB)).thenReturn(true);
        
        when(metaprojectAgent.isOperationAllowed(Operations.REMOVE_IMPORT.getId(), projectId, userIdA)).thenReturn(false);
        when(metaprojectAgent.isOperationAllowed(Operations.REMOVE_IMPORT.getId(), projectId, userIdB)).thenReturn(true);
        
        when(metaprojectAgent.isOperationAllowed(Operations.MODIFY_ONTOLOGY_IRI.getId(), projectId, userIdA)).thenReturn(true);
        when(metaprojectAgent.isOperationAllowed(Operations.MODIFY_ONTOLOGY_IRI.getId(), projectId, userIdB)).thenReturn(false);
        
        System.setProperty(ServerActivator.SERVER_CONFIGURATION_PROPERTY, "server-configuration.json");
        
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
        thrown.expectCause(new CauseMatcher(OperationNotAllowedException.class,
                "User has no permission for 'Remove ontology import', 'Add ontology import' operations"));
        
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
        thrown.expectCause(new CauseMatcher(OperationNotAllowedException.class,
                "User has no permission for 'Remove axiom', 'Remove ontology annotation', 'Modify the ontology IRI' operations"));
        
        policyFilter.commit(tokenUserB, projectId, commitBundle);
    }
}
