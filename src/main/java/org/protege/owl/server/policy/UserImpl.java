package org.protege.owl.server.policy;

import org.protege.owl.server.api.User;

public class UserImpl implements User {
    private String name;
    private String secret;

    public UserImpl(String name, String secret) {
        this.name = name;
        this.secret = secret;
    }

    @Override
    public String getUserName() {
        return name;
    }
    
    public String getSecret() {
        return secret;
    }

}
