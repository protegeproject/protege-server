package org.protege.editor.owl.server;

import static org.mockito.Mockito.when;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.policy.AccessControlFilter;
import org.protege.editor.owl.server.policy.CommitBundleImpl;
import org.protege.editor.owl.server.versioning.DocumentRevision;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;

import java.util.ArrayList;
import java.util.List;

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
        
        when(configuration.getMetaproject()).thenReturn(metaproject);
        when(metaproject.getMetaprojectAgent()).thenReturn(metaprojectAgent);
        
        when(metaprojectAgent.isOperationAllowed(Operations.ADD_AXIOM.getId(), projectId, userIdA)).thenReturn(true);
        when(metaprojectAgent.isOperationAllowed(Operations.ADD_AXIOM.getId(), projectId, userIdB)).thenReturn(true);
        
        when(metaprojectAgent.isOperationAllowed(Operations.REMOVE_AXIOM.getId(), projectId, userIdA)).thenReturn(true);
        when(metaprojectAgent.isOperationAllowed(Operations.REMOVE_AXIOM.getId(), projectId, userIdB)).thenReturn(false);
        
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
}
