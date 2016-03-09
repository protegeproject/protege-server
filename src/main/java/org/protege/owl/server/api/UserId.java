package org.protege.owl.server.api;

import java.io.Serializable;

@Deprecated
public final class UserId implements Comparable<UserId>, Serializable {
    private static final long serialVersionUID = -3698547861609983363L;
    private String userName;
    
    public UserId(String userName) {
        this.userName = userName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserId)) {
            return false;
        }
        return userName.equals(((UserId) obj).getUserName());
    }

    @Override
    public int hashCode() {
        return userName.hashCode()/42;
    }
    
    @Override
    public String toString() {
        return userName;
    }
    
    @Override
    public int compareTo(UserId o) {
        return userName.compareTo(o.getUserName());
    }
}
