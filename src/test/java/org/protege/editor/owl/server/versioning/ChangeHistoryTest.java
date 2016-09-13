package org.protege.editor.owl.server.versioning;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
@RunWith(MockitoJUnitRunner.class)
public class ChangeHistoryTest {

    @Mock private OWLOntologyChange mockChange;

    @Mock private RevisionMetadata firstMetadata;
    @Mock private RevisionMetadata secondMetadata;
    @Mock private RevisionMetadata thirdMetadata;

    private final List<OWLOntologyChange> firstChanges = Arrays.asList(mockChange, mockChange, mockChange);
    private final List<OWLOntologyChange> secondChanges = Arrays.asList(mockChange, mockChange);
    private final List<OWLOntologyChange> thirdChanges = Arrays.asList(mockChange);

    @Test
    public void canCreateEmptyChangeHistory() {
        ChangeHistory ch = ChangeHistoryImpl.createEmptyChangeHistory();
        assertThat(ch, is(notNullValue()));
        assertThat(ch.isEmpty(), is(true));
        assertThat(ch.getBaseRevision().getRevisionNumber(), is(0));
        assertThat(ch.getHeadRevision().getRevisionNumber(), is(0));
        assertThat(ch.getMetadata().isEmpty(), is(true));
        assertThat(ch.getRevisions().isEmpty(), is(true));
    }

    @Test
    public void canCreateCustomEmptyChangeHistory() {
        ChangeHistory ch5 = ChangeHistoryImpl.createEmptyChangeHistory(5);
        assertThat(ch5, is(notNullValue()));
        assertThat(ch5.isEmpty(), is(true));
        assertThat(ch5.getBaseRevision().getRevisionNumber(), is(5));
        assertThat(ch5.getHeadRevision().getRevisionNumber(), is(5));
        assertThat(ch5.getMetadata().isEmpty(), is(true));
        assertThat(ch5.getRevisions().isEmpty(), is(true));
    }

    @Test
    public void canAddRevision() {
        ChangeHistory ch = ChangeHistoryImpl.createEmptyChangeHistory();
        ch.addRevision(firstMetadata, firstChanges);
        assertThat(ch.getBaseRevision().getRevisionNumber(), is(0));
        assertThat(ch.getHeadRevision().getRevisionNumber(), is(1));
        assertThat(ch.getMetadata().isEmpty(), is(false));
        assertThat(ch.getMetadata().size(), is(1));
        assertThat(ch.getRevisions().isEmpty(), is(false));
        assertThat(ch.getRevisions().size(), is(1));
        
        ch.addRevision(secondMetadata, secondChanges);
        assertThat(ch.getBaseRevision().getRevisionNumber(), is(0));
        assertThat(ch.getHeadRevision().getRevisionNumber(), is(2));
        assertThat(ch.getMetadata().size(), is(2));
        assertThat(ch.getRevisions().size(), is(2));
    }

    @Test
    public void canReadRevision() {
        ChangeHistory ch = ChangeHistoryImpl.createEmptyChangeHistory();
        ch.addRevision(firstMetadata, firstChanges);
        ch.addRevision(secondMetadata, secondChanges);
        ch.addRevision(thirdMetadata, thirdChanges);
        
        when(firstMetadata.getAuthorName()).thenReturn("john");
        when(secondMetadata.getAuthorName()).thenReturn("john");
        when(thirdMetadata.getAuthorName()).thenReturn("mary");
        DocumentRevision r0 = DocumentRevision.create(0);
        DocumentRevision r1 = DocumentRevision.create(1);
        DocumentRevision r3 = DocumentRevision.create(3);
        DocumentRevision r5 = DocumentRevision.create(5);
        
        assertThat(ch.getHeadRevision().getRevisionNumber(), is(3));
        assertThat(ch.getMetadata().size(), is(3));
        assertThat(ch.getRevisions().size(), is(3));
        assertThat(ch.getMetadataForRevision(r0), is(nullValue()));
        assertThat(ch.getMetadataForRevision(r1).getAuthorName(), is("john"));
        assertThat(ch.getMetadataForRevision(r3).getAuthorName(), is("mary"));
        assertThat(ch.getMetadataForRevision(r5), is(nullValue()));
        assertThat(ch.getChangesForRevision(r0), is(nullValue()));
        assertThat(ch.getChangesForRevision(r1).size(), is(3));
        assertThat(ch.getChangesForRevision(r3).size(), is(1));
        assertThat(ch.getChangesForRevision(r5), is(nullValue()));
    }
}
