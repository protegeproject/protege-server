package org.protege.editor.owl.server.security;

import edu.stanford.protege.metaproject.api.*;
import edu.stanford.protege.metaproject.api.exception.UnknownUserIdException;
import edu.stanford.protege.metaproject.api.exception.UserNotRegisteredException;
import edu.stanford.protege.metaproject.impl.AuthorizedUserToken;
import org.protege.editor.owl.server.api.exception.ServerServiceException;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultLoginService implements SaltedChallengeLoginService {
    private final ServerConfiguration config;

    private SessionManager sessionManager;

    public DefaultLoginService(ServerConfiguration config, SessionManager sessionManager) {
        this.config = checkNotNull(config);
        this.sessionManager = sessionManager;
    }

    @Override
    public AuthToken login(UserId username, Password password) throws ServerServiceException {
        if (password instanceof SaltedPasswordDigest) {
            SaltedPasswordDigest saltedPassword = (SaltedPasswordDigest) password;
            return login(username, saltedPassword);
        }
        else {
            throw new ServerServiceException("Unsupported password object type: " + password.getClass());
        }
    }

    @Override
    public AuthToken login(UserId userId, SaltedPasswordDigest password) throws ServerServiceException {
        try {
            if (config.hasValidCredentials(userId, password)) {
                User user = config.getUser(userId);
                AuthToken authToken = new AuthorizedUserToken(user);
                sessionManager.add(authToken);
                return authToken;
            }
            throw new ServerServiceException("Invalid combination of username and password");
        }
        catch (UserNotRegisteredException e) {
            throw new ServerServiceException("Invalid combination of username and password");
        }
        catch (UnknownUserIdException e) {
            throw new ServerServiceException("Bad error. User has the credential but not registered", e);
        }

    }

    @Override
    public Salt getSalt(UserId userId) throws ServerServiceException {
        try {
            return config.getSalt(userId);
        }
        catch (UserNotRegisteredException e) {
            throw new ServerServiceException("Unknown user id: " + userId.get(), e);
        }
    }
}
