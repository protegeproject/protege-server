package org.protege.editor.owl.server.change;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
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
public class ChangeDocumentPoolEntryTest {

    private static final String historyFilename = "Koala.history";

    private HistoryFile historyFile;

    @Before
    public void setup() throws Exception {
        ClassLoader classloader = ChangeDocumentPoolEntry.class.getClassLoader();
        File historyFile = new File(classloader.getResource(historyFilename).getFile());
        historyFile = HistoryFile.openExisting(historyFile.getPath());
    }

    @Test
    public void canReadChangeHistory() throws IOException {
        ChangeDocumentPoolEntry entry = new ChangeDocumentPoolEntry(historyFile);
        ChangeHistory changeHistory = entry.getChangeHistory();
        assertThat(changeHistory, is(notNullValue()));
    }

    @Test
    public void canReadHead() throws IOException {
        ChangeDocumentPoolEntry entry = new ChangeDocumentPoolEntry(historyFile);
        DocumentRevision headRevision = entry.getHead();
        assertThat(headRevision, is(notNullValue()));
        assertThat(headRevision.getRevisionNumber(), is(0));
    }
}
