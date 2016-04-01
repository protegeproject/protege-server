package org.protege.owl.server.changes;

import java.io.Serializable;
import java.util.Date;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;

public final class ChangeMetaData implements Serializable {

    private static final long serialVersionUID = -1198003999159038367L;

    private Date date;
    private String comment;
    private User author;
    private UserId userId;

    public ChangeMetaData(Date date, String comment) {
        this.date = date;
        this.comment = comment;
    }

    public ChangeMetaData(String comment) {
        this.date = new Date();
        this.comment = comment;
    }

    public ChangeMetaData() {
        this.date = new Date();
        this.comment = "";
    }

    public Date getDate() {
        return date;
    }

    public String getCommitComment() {
        return comment;
    }

    @Deprecated
    public org.protege.owl.server.api.UserId getUserID() {
        return null;
    }

    @Deprecated
    public UserId getUserId() {
        return userId;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Deprecated
    public void setUser(AuthToken authToken) {
        this.userId = authToken.getUser().getId();
    }

    @Override
    public int hashCode() {
        return date.hashCode() + 42 * comment.hashCode() + author.hashCode() / 42;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChangeMetaData)) {
            return false;
        }
        ChangeMetaData other = (ChangeMetaData) obj;
        return other.getCommitComment().equals(comment) && other.getDate().equals(date)
                && other.getAuthor().equals(author);
    }

    @Override
    public String toString() {
        return "Committed at " + date + ": " + comment;
    }
}
