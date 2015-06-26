package org.protege.owl.server.api;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

public final class User implements Comparable<User>, Serializable {
    private static final long serialVersionUID = -3698547861609983363L;
    private String username, email;

    public User(String username) {
        this(username, "");
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }


    public String getEmail() {
        return email;
    }

    @Override
    public int compareTo(User o) {
        return ComparisonChain.start()
                .compare(this.username, o.username)
                .result();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("username", username)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equal(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
