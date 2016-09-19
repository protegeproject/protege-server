package org.protege.editor.owl.server.versioning;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class DocumentRevisionTest {

    @Test
    public void shouldBeEqualForSameRevisionNumber() {
        DocumentRevision newRevision = DocumentRevision.create(2);
        DocumentRevision otherRevision = DocumentRevision.create(2);
        assertThat(newRevision, is(otherRevision));
    }

    @Test
    public void shouldNotBeEqualForDifferentRevisionNumber() {
        DocumentRevision newRevision = DocumentRevision.create(2);
        DocumentRevision otherRevision = DocumentRevision.create(22);
        assertThat(newRevision, is(not(otherRevision)));
    }

    @Test
    public void shouldNotBeEqualForDifferentObject() {
        DocumentRevision newRevision = DocumentRevision.create(2);
        String otherRevision = "#22";
        assertThat(newRevision, is(not(otherRevision)));
    }

    @Test
    public void canCreate() {
        DocumentRevision r0 = DocumentRevision.create(0);
        assertThat(r0.getRevisionNumber(), is(0));
        
        DocumentRevision r10 = DocumentRevision.create(10);
        assertThat(r10.getRevisionNumber(), is(10));
        
        DocumentRevision rev = DocumentRevision.create(Integer.MAX_VALUE);
        assertThat(rev.getRevisionNumber(), is(Integer.MAX_VALUE));
    }

    @Test
    public void canCountDistance() {
        DocumentRevision r0 = DocumentRevision.create(0);
        DocumentRevision r5 = DocumentRevision.create(5);
        DocumentRevision r10 = DocumentRevision.create(10);
        
        assertThat(DocumentRevision.distance(r0, r5), is(5));
        assertThat(DocumentRevision.distance(r0, r10), is(10));
        assertThat(DocumentRevision.distance(r5, r0), is(-5));
        assertThat(DocumentRevision.distance(r5, r5), is(0));
    }

    @Test
    public void canKnowRelativePosition() {
        DocumentRevision r5 = DocumentRevision.create(5);
        DocumentRevision r10 = DocumentRevision.create(10);
        
        assertThat(r5.aheadOf(r10), is(false));
        assertThat(r10.aheadOf(r5), is(true));
        assertThat(r5.behind(r10), is(true));
        assertThat(r10.behind(r5), is(false));
        assertThat(r5.behindOrSameAs(r10), is(true));
        assertThat(r10.behindOrSameAs(r5), is(false));
        assertThat(r10.behindOrSameAs(r10), is(true));
        assertThat(r5.sameAs(r5), is(true));
        assertThat(r5.sameAs(r10), is(false));
    }

    @Test
    public void canNavigate() {
        DocumentRevision r5 = DocumentRevision.create(5);
        
        assertThat(r5.next().getRevisionNumber(), is(6));
        assertThat(r5.next(3).getRevisionNumber(), is(8));
        assertThat(r5.previous().getRevisionNumber(), is(4));
        assertThat(r5.previous(3).getRevisionNumber(), is(2));
    }
}
