package org.protege.editor.owl.server;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.exception.OperationNotAllowedException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.policy.AccessControlFilter;
import org.protege.editor.owl.server.policy.CommitBundleImpl;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Metaproject;
import edu.stanford.protege.metaproject.api.MetaprojectAgent;
import edu.stanford.protege.metaproject.api.Operation;
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

    @Mock private DocumentRevision headRevision;

    @Mock private Metaproject metaproject;
    @Mock private ServerConfiguration configuration;
    @Mock private MetaprojectAgent metaprojectAgent;

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
        
        baseServer = new ProtegeServer(configuration);
        policyFilter = new AccessControlFilter(baseServer);
    }

    @Test
    public void authorizeCommitTest() throws ServerServiceException {
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ontology, axiom2));
        changes.add(new AddAxiom(ontology, axiom3));
        changes.add(new RemoveAxiom(ontology, axiom1));
        CommitBundle commits = new CommitBundleImpl(changes, headRevision);
        policyFilter.commit(tokenUserA, projectX, commits);
    }

    @Test(expected=ServerServiceException.class)
    public void unauthorizeCommitTest() throws ServerServiceException {
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ontology, axiom2));
        changes.add(new AddAxiom(ontology, axiom3));
        changes.add(new RemoveAxiom(ontology, axiom1));
        CommitBundle commits = new CommitBundleImpl(changes, headRevision);
        policyFilter.commit(tokenUserB, projectX, commits);
    }

    @Test(expected=ServerServiceException.class)
    public void unauthorizeOperationsUserATest() throws ServerServiceException {
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ontology, axiom2));
        changes.add(new RemoveAxiom(ontology, axiom1));
        changes.add(new AddOntologyAnnotation(ontology, annotation2));
        changes.add(new RemoveOntologyAnnotation(ontology, annotation1));
        changes.add(new AddImport(ontology, importDecl2));
        changes.add(new RemoveImport(ontology, importDecl1));
        changes.add(new SetOntologyID(ontology, otherOntologyId));
        CommitBundle commits = new CommitBundleImpl(changes, headRevision);
        
        try {
            policyFilter.commit(tokenUserA, projectX, commits);
        }
        catch (ServerServiceException e) {
            OperationNotAllowedException onae = (OperationNotAllowedException) e.getCause();
            Set<Operation> unauthorizedOperations = onae.getOperations();
            assertThat(unauthorizedOperations, containsInAnyOrder(Operations.ADD_IMPORT, Operations.REMOVE_IMPORT));
            throw e;
        }
    }

    @Test(expected=ServerServiceException.class)
    public void unauthorizeOperationsUserBTest() throws ServerServiceException {
        List<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ontology, axiom2));
        changes.add(new RemoveAxiom(ontology, axiom1));
        changes.add(new AddOntologyAnnotation(ontology, annotation2));
        changes.add(new RemoveOntologyAnnotation(ontology, annotation1));
        changes.add(new AddImport(ontology, importDecl2));
        changes.add(new RemoveImport(ontology, importDecl1));
        changes.add(new SetOntologyID(ontology, otherOntologyId));
        CommitBundle commits = new CommitBundleImpl(changes, headRevision);
        
        try {
            policyFilter.commit(tokenUserB, projectX, commits);
        }
        catch (ServerServiceException e) {
            OperationNotAllowedException onae = (OperationNotAllowedException) e.getCause();
            Set<Operation> unauthorizedOperations = onae.getOperations();
            assertThat(unauthorizedOperations,
                    containsInAnyOrder(Operations.REMOVE_AXIOM, Operations.REMOVE_ONTOLOGY_ANNOTATION, Operations.MODIFY_ONTOLOGY_IRI));
            throw e;
        }
    }
}
