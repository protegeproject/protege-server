package org.protege.owl.server.api;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

public final class User implements Comparable<User>, Serializable {
    private static final long serialVersionUID = -3698547861609983363L;
    private String userName;

    public User(String userName) {
        this.userName = userName;
    }
    
    public String getUserName() {
        return userName;
    }

    @Override
    public int compareTo(User o) {
        return ComparisonChain.start()
                .compare(this.userName, o.userName)
                .result();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userName", userName)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equal(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userName);
    }
}
