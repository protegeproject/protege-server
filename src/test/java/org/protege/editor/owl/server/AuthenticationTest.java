package org.protege.editor.owl.server;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.security.DefaultLoginService;
import org.protege.editor.owl.server.security.SessionManager;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.AuthenticationRegistry;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.UserRegistry;
import edu.stanford.protege.metaproject.api.exception.UserNotRegisteredException;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTest {

    @Mock private UserId validUserId;
    @Mock private User validUser;
    @Mock private SaltedPasswordDigest validSaltedPassword;

    @Mock private UserId invalidUserId;
    @Mock private User invalidUser;
    @Mock private SaltedPasswordDigest invalidSaltedPassword;

    @Mock private Salt userSalt;

    @Mock private AuthenticationRegistry authRegistry;
    @Mock private UserRegistry userRegistry;
    @Mock private SessionManager sessionManager;

    private DefaultLoginService loginService;

    @Before
    public void setUp() throws Exception {
        loginService = new DefaultLoginService(authRegistry, userRegistry, sessionManager);
        
        when(authRegistry.hasValidCredentials(validUserId, validSaltedPassword)).thenReturn(true);
        when(userRegistry.get(validUserId)).thenReturn(validUser);
        
        when(authRegistry.hasValidCredentials(invalidUserId, invalidSaltedPassword)).thenReturn(false);
        when(authRegistry.hasValidCredentials(invalidUserId, validSaltedPassword)).thenReturn(false);
        when(authRegistry.hasValidCredentials(validUserId, invalidSaltedPassword)).thenReturn(false);
        
        when(authRegistry.getSalt(validUserId)).thenReturn(userSalt);
        when(authRegistry.getSalt(invalidUserId)).thenThrow(new UserNotRegisteredException());
    }

    @Test
    public void loginTest() throws Exception {
        AuthToken token = loginService.login(validUserId, validSaltedPassword);
        assertNotNull(token);
    }

    @Test(expected=ServerServiceException.class)
    public void invalidLoginTest() throws Exception {
        AuthToken token = loginService.login(invalidUserId, invalidSaltedPassword);
        assertNull(token);
    }

    @Test(expected=ServerServiceException.class)
    public void invalidUsernameTest() throws Exception {
        AuthToken token = loginService.login(invalidUserId, validSaltedPassword);
        assertNull(token);
    }

    @Test(expected=ServerServiceException.class)
    public void invalidPasswordTest() throws Exception {
        AuthToken token = loginService.login(validUserId, invalidSaltedPassword);
        assertNull(token);
    }

    @Test
    public void getEncryptionKeyTest() throws Exception {
        Salt salt = loginService.getSalt(validUserId);
        assertNotNull(salt);
    }

    @Test(expected=ServerServiceException.class)
    public void notGetEncryptionKeyTest() throws Exception {
        Salt salt = loginService.getSalt(invalidUserId);
        assertNull(salt);
    }
    
}
