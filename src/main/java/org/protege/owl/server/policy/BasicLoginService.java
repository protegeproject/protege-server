package org.protege.owl.server.policy;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.UserId;

public class BasicLoginService implements LoginService {
    private UserDatabase userDb;
    
    public BasicLoginService(UserDatabase userDb) {
        this.userDb = userDb;
    }

    public AuthToken login(String name, String password) {
        UserId u = new UserId(name);
        if (!userDb.checkPassword(u, password)) {
            return null;
        }
        SimpleAuthToken authToken = userDb.getToken(u);
        if (authToken == null) {
            authToken = new SimpleAuthToken(u);
            userDb.addToken(authToken);
        }
        return authToken;
    }
    
    public boolean checkAuthentication(AuthToken token) {
        if (!(token instanceof SimpleAuthToken)) {
            return false;
        }
        return userDb.isValid((SimpleAuthToken) token);
    }
}
