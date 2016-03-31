package org.protege.owl.server.security;

import org.protege.owl.server.api.LoginService;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.AuthenticationRegistry;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.UserNotRegisteredException;
import edu.stanford.protege.metaproject.impl.AuthorizedUserToken;

public class DefaultLoginService implements LoginService, SimpleHashProtocol {

    private AuthenticationRegistry authRegistry;
    private SessionManager sessionManager;

    public DefaultLoginService(AuthenticationRegistry authRegistry, SessionManager sessionManager) {
        this.authRegistry = authRegistry;
        this.sessionManager = sessionManager;
    }

    @Override
    public AuthToken login(UserId userId, SaltedPasswordDigest password) throws Exception {
        if (authRegistry.hasValidCredentials(userId, password)) {
            AuthToken authToken = new AuthorizedUserToken(userId);
            sessionManager.add(authToken);
            return authToken;
        }
        throw new Exception("Invalid combination of username and password");
    }

    @Override
    public Salt getSalt(UserId userId) throws UserNotRegisteredException {
        return authRegistry.getSalt(userId);
    }
}
