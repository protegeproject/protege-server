package org.protege.owl.server.policy;

import java.rmi.RemoteException;
import java.util.UUID;

import org.protege.owl.server.api.AuthToken;

public class BasicLoginService implements LoginService {
    private UserDatabase userDb;
    
    public BasicLoginService(UserDatabase userDb) {
        this.userDb = userDb;
    }

    public AuthToken login(String name, String password) throws RemoteException {
        SimpleAuthToken u = userDb.getUser(name);
        if (u != null && u.getPassword().equals(password)) {
            if (u.getSecret() == null) {
                u.setSecret(UUID.randomUUID().toString());
            }
            return u;
        }
        return null;
    }
    
    public boolean checkAuthentication(AuthToken token) {
        if (!(token instanceof SimpleAuthToken)) {
            return false;
        }
        SimpleAuthToken authenticatedId = userDb.getUser(token.getUserId().getUserName());
        return authenticatedId.getSecret() != null && authenticatedId.getSecret().equals(((SimpleAuthToken) token).getSecret());
    }
}
