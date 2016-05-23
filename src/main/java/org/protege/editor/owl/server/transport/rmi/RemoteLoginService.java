package org.protege.editor.owl.server.transport.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface RemoteLoginService extends Remote {

    AuthToken login(UserId username, SaltedPasswordDigest password) throws RemoteException;

    Salt getSalt(UserId userId) throws RemoteException;
}
