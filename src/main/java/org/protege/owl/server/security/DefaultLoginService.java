package org.protege.owl.server.security;

import org.protege.owl.server.api.RmiLoginService;

import java.rmi.RemoteException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.AuthenticationManager;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.UserNotRegisteredException;

public class DefaultLoginService implements RmiLoginService {

    private AuthenticationManager authManager;
    private SessionManager sessionManager;

    public DefaultLoginService(AuthenticationManager authManager, SessionManager sessionManager) {
        this.authManager = authManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public AuthToken login(UserId userId, SaltedPasswordDigest password) throws RemoteException {
        return null;
    }

    public Salt getSalt(UserId userId) throws UserNotRegisteredException {
        return authManager.getSalt(userId);
    }
}
