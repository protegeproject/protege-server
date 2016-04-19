package org.protege.owl.server.security;

import org.protege.owl.server.api.LoginService;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.AuthenticationRegistry;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.UserRegistry;
import edu.stanford.protege.metaproject.impl.AuthorizedUserToken;

public class DefaultLoginService implements LoginService {

    private AuthenticationRegistry authRegistry;
    private UserRegistry userRegistry;
    private SessionManager sessionManager;

    public DefaultLoginService(AuthenticationRegistry authRegistry, UserRegistry userRegistry, SessionManager sessionManager) {
        this.authRegistry = authRegistry;
        this.userRegistry = userRegistry;
        this.sessionManager = sessionManager;
    }

    @Override
    public AuthToken login(UserId userId, SaltedPasswordDigest password) throws Exception {
        if (authRegistry.hasValidCredentials(userId, password)) {
            User user = userRegistry.get(userId);
            AuthToken authToken = new AuthorizedUserToken(user);
            sessionManager.add(authToken);
            return authToken;
        }
        throw new Exception("Invalid combination of username and password");
    }

    @Override
    public Salt getEncryptionKey(UserId userId) throws Exception {
        return authRegistry.getSalt(userId);
    }
}
