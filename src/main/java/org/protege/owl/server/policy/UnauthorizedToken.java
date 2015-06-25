package org.protege.owl.server.policy;

import java.io.Serializable;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.User;

public class UnauthorizedToken implements AuthToken, Serializable {
    private static final long serialVersionUID = 5535482187202979494L;
    
    private String userName;
    
    public UnauthorizedToken(String userName) {
        this.userName = userName;
    }

    @Override
    public int compareTo(AuthToken o) {
        return getUser().compareTo(o.getUser());
    }

    @Override
    public User getUser() {
        return new User(userName);
    }

}
