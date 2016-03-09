package org.protege.owl.server.policy;

import java.io.Serializable;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.UserId;

@Deprecated
public class UnauthorizedToken implements AuthToken, Serializable {
    private static final long serialVersionUID = 5535482187202979494L;
    
    private String userName;
    
    public UnauthorizedToken(String userName) {
        this.userName = userName;
    }

    @Override
    public int compareTo(AuthToken o) {
        return getUserId().compareTo(o.getUserId());
    }

    @Override
    public UserId getUserId() {
        return new UserId(userName);
    }

}
