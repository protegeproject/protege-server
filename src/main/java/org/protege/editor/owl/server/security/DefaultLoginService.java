package org.protege.editor.owl.server.security;

import org.protege.editor.owl.server.api.exception.ServerServiceException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.AuthenticationRegistry;
import edu.stanford.protege.metaproject.api.Password;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.UserRegistry;
import edu.stanford.protege.metaproject.api.exception.UnknownMetaprojectObjectIdException;
import edu.stanford.protege.metaproject.api.exception.UserNotRegisteredException;
import edu.stanford.protege.metaproject.impl.AuthorizedUserToken;

public class DefaultLoginService implements SaltedChallengeLoginService {

    private AuthenticationRegistry authRegistry;
    private UserRegistry userRegistry;
    private SessionManager sessionManager;

    public DefaultLoginService(AuthenticationRegistry authRegistry, UserRegistry userRegistry, SessionManager sessionManager) {
        this.authRegistry = authRegistry;
        this.userRegistry = userRegistry;
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
            if (authRegistry.hasValidCredentials(userId, password)) {
                User user = userRegistry.get(userId);
                AuthToken authToken = new AuthorizedUserToken(user);
                sessionManager.add(authToken);
                return authToken;
            }
            throw new ServerServiceException("Invalid combination of username and password");
        }
        catch (UserNotRegisteredException e) {
            throw new ServerServiceException("Invalid combination of username and password");
        }
        catch (UnknownMetaprojectObjectIdException e) {
            throw new ServerServiceException("Bad error. User has the credential but not registered", e);
        }

    }

    @Override
    public Salt getSalt(UserId userId) throws ServerServiceException {
        try {
            return authRegistry.getSalt(userId);
        }
        catch (UserNotRegisteredException e) {
            throw new ServerServiceException("Unknown user id: " + userId.get(), e);
        }
    }
}
