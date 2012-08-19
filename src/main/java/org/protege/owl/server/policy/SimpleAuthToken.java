package org.protege.owl.server.policy;

import java.io.Serializable;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.UserId;

public class SimpleAuthToken implements AuthToken, Serializable {
    private static final long serialVersionUID = -3590024420017662281L;
    private String name;
    private transient String password;
    private String secret;

    public SimpleAuthToken(String name, String password) {
        this.name = name;
        this.password = password;
    }

    @Override
    public UserId getUserId() {
        return new UserId(name);
    }
    
    // ToDo remove this.
    public String getPassword() {
        return password;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
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
        if (!(obj instanceof AuthToken)) {
            return false;
        }
        AuthToken other = (AuthToken) obj;
        return name.equals(other.getUserId().getUserName());
    }
    
    @Override
    public int compareTo(AuthToken other) {
        return name.compareTo(other.getUserId().getUserName());
    }
    
    @Override
    public String toString() {
        return "[User: " + name + "]";
    }

}
