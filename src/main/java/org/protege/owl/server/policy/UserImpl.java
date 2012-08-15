package org.protege.owl.server.policy;

import java.io.Serializable;

import org.protege.owl.server.api.User;

public class UserImpl implements User, Serializable {
    private static final long serialVersionUID = -3590024420017662281L;
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
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (UserImpl) obj;
        return name.equals(other.getUserName());
    }
    
    @Override
    public String toString() {
        return "[User: " + name + "]";
    }

}
