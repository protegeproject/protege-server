package org.protege.editor.owl.server.change;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ChangeDocumentPoolTest {

    private static final String historyFilename = "Koala.history";

    private ChangeDocumentPool documentPool;

    private HistoryFile historyFile;

    @Before
    public void setup() throws Exception {
        documentPool = new ChangeDocumentPool(8000); // timeout = 8 secs
        ClassLoader classloader = ChangeDocumentPoolEntry.class.getClassLoader();
        String filepath = classloader.getResource(historyFilename).getPath();
        historyFile = HistoryFile.openExisting(filepath);
    }

    @Test
    public void canLookup() throws IOException {
        ChangeHistory changeHistory = documentPool.lookup(historyFile);
        assertThat(changeHistory, is(notNullValue()));
    }

    @Test
    public void canLookupHead() throws IOException {
        DocumentRevision headRevision = documentPool.lookupHead(historyFile);
        assertThat(headRevision, is(notNullValue()));
        assertThat(headRevision.getRevisionNumber(), is(0));
    }
}
