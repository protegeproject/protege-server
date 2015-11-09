package org.protege.owl.server.policy;

import java.io.Serializable;
import java.util.UUID;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.UserId;

public final class SimpleAuthToken implements AuthToken, Serializable {
    private static final long serialVersionUID = -3590024420017662281L;
    private UserId userId;
    private String secret;

    public SimpleAuthToken(UserId userId) {
        this.userId = userId;
        this.secret = UUID.randomUUID().toString();
    }

    @Override
    public UserId getUserId() {
        return userId;
    }
    
    @Override
    public int hashCode() {
        return userId.hashCode() + 2718 * secret.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleAuthToken)) {
            return false;
        }
        SimpleAuthToken other = (SimpleAuthToken) obj;
        return userId.equals(other.getUserId()) && secret.equals(other.secret);
    }
    
    @Override
    public int compareTo(AuthToken other) {
        return userId.compareTo(other.getUserId());
    }
    
    @Override
    public String toString() {
        return "<Authentication Token for " + userId + ">";
    }

}
