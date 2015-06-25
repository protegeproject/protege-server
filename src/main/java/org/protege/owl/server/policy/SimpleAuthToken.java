package org.protege.owl.server.policy;

import java.io.Serializable;
import java.util.UUID;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.User;

public final class SimpleAuthToken implements AuthToken, Serializable {
    private static final long serialVersionUID = -3590024420017662281L;
    private User user;
    private String secret;

    public SimpleAuthToken(User user) {
        this.user = user;
        this.secret = UUID.randomUUID().toString();
    }

    @Override
    public User getUser() {
        return user;
    }
    
    @Override
    public int hashCode() {
        return user.hashCode() + 2718 * secret.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleAuthToken)) {
            return false;
        }
        SimpleAuthToken other = (SimpleAuthToken) obj;
        return user.equals(other.getUser()) && secret.equals(other.secret);
    }
    
    @Override
    public int compareTo(AuthToken other) {
        return user.compareTo(other.getUser());
    }
    
    @Override
    public String toString() {
        return "<Authentication Token for " + user + ">";
    }

}
