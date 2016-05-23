package org.protege.editor.owl.server.transport.rmi;

import org.protege.editor.owl.server.api.LoginService;
import org.protege.editor.owl.server.security.SaltedChallengeLoginService;

import java.rmi.RemoteException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;

public class RmiLoginService implements RemoteLoginService {

    public static String LOGIN_SERVICE = "RmiLoginService";

    private SaltedChallengeLoginService loginService;

    public RmiLoginService(LoginService loginService) throws RemoteException {
        if (loginService instanceof SaltedChallengeLoginService) {
            this.loginService = (SaltedChallengeLoginService) loginService;
        }
        else {
            throw new RemoteException("Unable to setup the login protocol. Invalid type of login service.");
        }
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
        try {
            return loginService.getSalt(userId);
        }
        catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
