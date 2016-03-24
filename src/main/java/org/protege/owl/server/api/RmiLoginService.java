package org.protege.owl.server.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.LoginService;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;

public interface RmiLoginService extends LoginService, Remote {

    AuthToken login(UserId username, SaltedPasswordDigest password) throws RemoteException;
}
