package org.protege.owl.server.security;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.UserId;

/**
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class AuthorizedUserToken implements AuthToken, Serializable {
    private static final long serialVersionUID = -7075644503123519614L;
    private final UserId userId;
    private String secret;

    /**
     * Constructor
     *
     * @param userId    User identifier
     */
    public AuthorizedUserToken(UserId userId) {
        this.userId = checkNotNull(userId);
        this.secret = UUID.randomUUID().toString();
    }

    @Override
    public UserId getUserId() {
        return userId;
    }

    @Override
    public boolean isAuthorized() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizedUserToken that = (AuthorizedUserToken) o;
        return Objects.equal(userId, that.userId) &&
                Objects.equal(secret, that.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, secret);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .toString();
    }

    @Override
    public int compareTo(@Nonnull AuthToken that) {
        return ComparisonChain.start()
                .compare(this.userId.get(), that.getUserId().get())
                .result();
    }
}
