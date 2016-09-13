package org.protege.editor.owl.server.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.protege.editor.owl.server.security.LoginTimeoutException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.User;
import junit.framework.TestCase;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
@RunWith(MockitoJUnitRunner.class)
public class TokenTableTest extends TestCase {

    @Mock private User user1;
    @Mock private User user2;

    @Mock private AuthToken authToken1;
    @Mock private AuthToken authToken2;

    private TokenTable tokenTable;

    @Before
    public void createTokenTable() {
        tokenTable = new TokenTable(8000); // timeout = 8 secs
    }

    @Test
    public void canPut() {
        tokenTable.put("aaa", authToken1);
        tokenTable.put("bbb", authToken2);
        assertThat(tokenTable.size(), is(2L));
    }

    @Test
    public void canGet() throws Exception {
        tokenTable.put("aaa", authToken1);
        tokenTable.put("bbb", authToken2);
        
        when(authToken1.getUser()).thenReturn(user1);
        when(authToken2.getUser()).thenReturn(user2);
        
        assertThat(tokenTable.get("aaa"), is(authToken1));
        assertThat(tokenTable.get("bbb"), is(authToken2));
    }

    @Test(expected=LoginTimeoutException.class)
    public void throwsLoginTimeoutException() throws Exception {
        tokenTable.put("aaa", authToken1);
        tokenTable.put("bbb", authToken2);
        
        when(authToken1.getUser()).thenReturn(user1);
        when(authToken2.getUser()).thenReturn(user2);
        
        Thread.sleep(10000); // sleep for 10 secs
        
        assertThat(tokenTable.get("aaa"), is(authToken1));
        assertThat(tokenTable.get("bbb"), is(authToken2));
    }
}
