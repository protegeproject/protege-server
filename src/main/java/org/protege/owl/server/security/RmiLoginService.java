package org.protege.owl.server.security;

import java.rmi.Remote;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.AuthenticationManager;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserAuthenticator;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.UserNotRegisteredException;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class RmiLoginService implements UserAuthenticator, Remote {

    public static String LOGIN_SERVICE = "RmiLoginService";

    AuthenticationManager authManager;
    SessionManager sessionManager;

    public RmiLoginService(AuthenticationManager authManager, SessionManager sessionManager) {
        this.authManager = authManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public AuthToken hasValidCredentials(UserId userId, SaltedPasswordDigest password) throws UserNotRegisteredException {
        return null;
    }

    public Salt getSalt(UserId userId) throws UserNotRegisteredException {
        return authManager.getSalt(userId);
    }
}
