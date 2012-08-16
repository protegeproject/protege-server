package org.protege.owl.server.policy;

import java.rmi.RemoteException;
import java.util.UUID;

import org.protege.owl.server.api.User;

public class BasicLoginService implements LoginService {
    private UserDatabase userDb;
    
    public BasicLoginService(UserDatabase userDb) {
        this.userDb = userDb;
    }

    public User login(String name, String password) throws RemoteException {
        UserExt u = userDb.getUser(name);
        if (u != null && u.getPassword().equals(password)) {
            if (u.getSecret() == null) {
                u.setSecret(UUID.randomUUID().toString());
            }
            return u;
        }
        return null;
    }
    
    public boolean checkAuthentication(User user) {
        if (!(user instanceof UserExt)) {
            return false;
        }
        UserExt authenticatedId = userDb.getUser(user.getUserName());
        return authenticatedId.getSecret() != null && authenticatedId.getSecret().equals(((UserExt) user).getSecret());
    }
}
