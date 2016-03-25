package org.protege.owl.server.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * Represents the login service provided by the server through RMI transport.
 *
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface RmiLoginService extends LoginService, Remote {

    public static String LOGIN_SERVICE = "RmiLoginService";

    AuthToken login(UserId username, SaltedPasswordDigest password) throws RemoteException;
}
