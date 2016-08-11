package org.protege.editor.owl.server;

import edu.stanford.protege.metaproject.api.*;
import edu.stanford.protege.metaproject.api.exception.UserNotRegisteredException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.security.DefaultLoginService;
import org.protege.editor.owl.server.security.SessionManager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

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

    @Mock private SessionManager sessionManager;

    @Mock private ServerConfiguration configuration;

    private DefaultLoginService loginService;

    @Before
    public void setUp() throws Exception {
        loginService = new DefaultLoginService();
        loginService.setConfig(configuration);
        
        when(configuration.hasValidCredentials(validUserId, validSaltedPassword)).thenReturn(true);
        when(configuration.getUser(validUserId)).thenReturn(validUser);
        
        when(configuration.hasValidCredentials(invalidUserId, invalidSaltedPassword)).thenReturn(false);
        when(configuration.hasValidCredentials(invalidUserId, validSaltedPassword)).thenReturn(false);
        when(configuration.hasValidCredentials(validUserId, invalidSaltedPassword)).thenReturn(false);
        
        when(configuration.getSalt(validUserId)).thenReturn(userSalt);
        when(configuration.getSalt(invalidUserId)).thenThrow(new UserNotRegisteredException());
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
