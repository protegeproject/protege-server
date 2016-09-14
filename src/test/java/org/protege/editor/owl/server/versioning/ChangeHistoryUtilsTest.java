package org.protege.editor.owl.server.versioning;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.RevisionMetadata;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ChangeHistoryUtilsTest {

    @Mock private OWLOntologyChange mockChange;

    @Mock private RevisionMetadata firstMetadata;
    @Mock private RevisionMetadata secondMetadata;
    @Mock private RevisionMetadata thirdMetadata;

    private final List<OWLOntologyChange> firstChanges = Arrays.asList(mockChange, mockChange, mockChange);
    private final List<OWLOntologyChange> secondChanges = Arrays.asList(mockChange, mockChange);
    private final List<OWLOntologyChange> thirdChanges = Arrays.asList(mockChange);

    private final DocumentRevision r0 = DocumentRevision.create(0);
    private final DocumentRevision r1 = DocumentRevision.create(1);
    private final DocumentRevision r3 = DocumentRevision.create(3);
    private final DocumentRevision r5 = DocumentRevision.create(5);

    private ChangeHistory changeHistory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        changeHistory = ChangeHistoryImpl.createEmptyChangeHistory();
        changeHistory.addRevision(firstMetadata, firstChanges);
        changeHistory.addRevision(secondMetadata, secondChanges);
        changeHistory.addRevision(thirdMetadata, thirdChanges);
    }

    @Test
    public void canCropFromStartRevision() {
        // operate
        ChangeHistory croppedHistory = ChangeHistoryUtils.crop(changeHistory, r0);
        // check
        assertThat(croppedHistory.getBaseRevision(), is(r0));
        assertThat(croppedHistory.getHeadRevision(), is(r3));
        assertThat(croppedHistory.getRevisions().size(), is(3));
    }

    @Test
    public void canCropFromMiddleRevision() {
        // operate
        ChangeHistory croppedHistory = ChangeHistoryUtils.crop(changeHistory, r1);
        // check
        assertThat(croppedHistory.getBaseRevision(), is(r1));
        assertThat(croppedHistory.getHeadRevision(), is(r3));
        assertThat(croppedHistory.getRevisions().size(), is(2));
    }

    @Test
    public void canCropFromEndRevision() {
        // operate
        ChangeHistory croppedHistory = ChangeHistoryUtils.crop(changeHistory, r3);
        // check
        assertThat(croppedHistory.getBaseRevision(), is(r3));
        assertThat(croppedHistory.getHeadRevision(), is(r3));
        assertThat(croppedHistory.getRevisions().size(), is(0));
    }

    @Test (expected=IllegalArgumentException.class)
    public void throwIllegalArgumentException_OutOfRange() {
        // operate
        ChangeHistoryUtils.crop(changeHistory, r5);
    }

    @Test
    public void canCropUsingRangedRevision() {
        // operate
        ChangeHistory croppedHistory = ChangeHistoryUtils.crop(changeHistory, r1, r3);
        // check
        assertThat(croppedHistory.getBaseRevision(), is(r1));
        assertThat(croppedHistory.getHeadRevision(), is(r3));
        assertThat(croppedHistory.getRevisions().size(), is(2));
    }

    @Test
    public void canCropUsingOffset() {
        // operate
        ChangeHistory croppedHistory = ChangeHistoryUtils.crop(changeHistory, r1, 2);
        // check
        assertThat(croppedHistory.getBaseRevision(), is(r1));
        assertThat(croppedHistory.getHeadRevision(), is(r3));
        assertThat(croppedHistory.getRevisions().size(), is(2));
    }
}
