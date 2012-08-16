package org.protege.owl.server.policy;

import java.io.Serializable;

import org.protege.owl.server.api.User;

public class UserExt implements User, Serializable {
    private static final long serialVersionUID = -3590024420017662281L;
    private String name;
    private transient String password;
    private String secret;

    public UserExt(String name, String password) {
        this.name = name;
        this.password = password;
    }

    @Override
    public String getUserName() {
        return name;
    }
    
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
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        return name.equals(other.getUserName());
    }
    
    @Override
    public int compareTo(User other) {
        return getUserName().compareTo(other.getUserName());
    }
    
    @Override
    public String toString() {
        return "[User: " + name + "]";
    }

}
