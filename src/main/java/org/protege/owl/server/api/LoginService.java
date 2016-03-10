package org.protege.owl.server.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface LoginService extends Remote { // TODO: Put it in the Metaproject API

    AuthToken login(UserId username, SaltedPasswordDigest password) throws RemoteException;
}
