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
import org.protege.editor.owl.server.security.TokenTable;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.impl.NameImpl;
import edu.stanford.protege.metaproject.impl.UserIdImpl;
import junit.framework.TestCase;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
@RunWith(MockitoJUnitRunner.class)
public class TokenTableTest extends TestCase {

    private static final long TIMEOUT_IN_MILISECONDS = 3000; // timeout = 3 secs

    @Mock
    private AuthToken authToken1, authToken2;

    @Mock
    private User user1, user2;

    private TokenTable tokenTable;

    @Before
    public void setUp() {
        when(authToken1.getUser()).thenReturn(user1);
        when(authToken2.getUser()).thenReturn(user2);
        when(user1.getId()).thenReturn(new UserIdImpl("user1"));
        when(user2.getId()).thenReturn(new UserIdImpl("user2"));
        when(user1.getName()).thenReturn(new NameImpl("User 1"));
        when(user2.getName()).thenReturn(new NameImpl("User 2"));
        when(authToken1.getUser()).thenReturn(user1);
        when(authToken2.getUser()).thenReturn(user2);
        createTokenTable();
    }

    private void createTokenTable() {
        tokenTable = TokenTable.create(TIMEOUT_IN_MILISECONDS);
    }

    @Test
    public void shouldStoreKeyValue() throws Exception {
        String key = "aaa";
        tokenTable.put(key, authToken1);
        assertThat(tokenTable.get(key), is(authToken1));
    }

    @Test
    public void shouldReturnTotalSize() throws Exception {
        String key1 = "aaa";
        String key2 = "bbb";
        tokenTable.put(key1, authToken1);
        tokenTable.put(key2, authToken2); // put another
        assertThat(tokenTable.size(), is(2));
    }

    @Test
    public void shouldReturnSizeZeroAfterTimeout() throws Exception {
        String key1 = "aaa";
        String key2 = "bbb";
        tokenTable.put(key1, authToken1);
        tokenTable.put(key2, authToken2);
        Thread.sleep(5000); // sleep for 5 secs
        assertThat(tokenTable.size(), is(0));
    }

    @Test
    public void shouldRestartTokenTimeout() throws Exception {
        String key = "aaa";
        tokenTable.put(key, authToken1);
        
        long lastCall = TIMEOUT_IN_MILISECONDS - 100;
        Thread.sleep(lastCall); // sleep for almost the last call timeout
        tokenTable.get(key);
        Thread.sleep(lastCall); // sleep again for almost the last call timeout
        
        assertThat(tokenTable.get(key), is(authToken1));
    }

    @Test(expected=LoginTimeoutException.class)
    public void throwsLoginTimeoutException() throws Exception {
        String key = "aaa";
        tokenTable.put(key, authToken1);
        Thread.sleep(5000); // sleep for 5 secs
        assertThat(tokenTable.get(key), is(authToken1));
    }

    @Test
    public void shouldMaintainOnlyActiveToken() throws Exception {
        String key1 = "aaa";
        String key2 = "bbb";
        tokenTable.put(key1, authToken1);
        tokenTable.put(key2, authToken2);
        
        long lastCall = TIMEOUT_IN_MILISECONDS - 100;
        Thread.sleep(lastCall);
        tokenTable.get(key2);
        Thread.sleep(lastCall);
        
        assertThat(tokenTable.size(), is(1));
        AuthToken activeToken = null;
        try {
            try {
                activeToken = tokenTable.get(key1);
            } catch (LoginTimeoutException e) {
                assertThat("Should throw LoginTimeoutException for token 'aaa'", true);
            }
            try {
                activeToken = tokenTable.get(key2);
            } catch (LoginTimeoutException e) {
                assertThat("Should NOT throw LoginTimeoutException for token 'bbb'", false);
            }
        }
        finally {
            assertThat(activeToken, is(authToken2));
        }
    }
}
