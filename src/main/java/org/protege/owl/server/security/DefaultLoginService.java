package org.protege.owl.server.security;

import org.protege.owl.server.api.RmiLoginService;

import java.rmi.RemoteException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class DefaultLoginService implements RmiLoginService {

    SessionManager sessionManager;

    public DefaultLoginService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public AuthToken login(UserId username, SaltedPasswordDigest password) throws RemoteException {
        return null;
    }
}
