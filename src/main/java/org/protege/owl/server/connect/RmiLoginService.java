package org.protege.owl.server.connect;

import org.protege.owl.server.api.LoginService;
import org.protege.owl.server.security.SimpleHashProtocol;

import java.rmi.RemoteException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;

public class RmiLoginService implements RemoteLoginService, SimpleHashProtocol {

    public static String LOGIN_SERVICE = "RmiLoginService";

    private LoginService loginService;

    public RmiLoginService(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public AuthToken login(UserId username, SaltedPasswordDigest password) throws RemoteException {
        try {
            return loginService.login(username, password);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Salt getSalt(UserId userId) throws RemoteException {
        if (loginService instanceof SimpleHashProtocol) {
            try {
                ((SimpleHashProtocol) loginService).getSalt(userId);
            }
            catch (Exception e) {
                throw new RemoteException(e.getMessage(), e);
            }
        }
        throw new RemoteException("Invalid protocol to generate password salt");
    }
}
