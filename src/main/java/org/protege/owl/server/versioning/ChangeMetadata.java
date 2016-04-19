package org.protege.owl.server.versioning;

import java.io.Serializable;
import java.util.Date;

import edu.stanford.protege.metaproject.api.User;
import edu.stanford.protege.metaproject.api.UserId;

public final class ChangeMetadata implements Serializable {

    private static final long serialVersionUID = -1198003999159038367L;

    private User author;
    private Date date;
    private String comment;

    public ChangeMetadata(User author, String comment) {
        this.author = author;
        this.date = new Date();
        this.comment = comment;
    }

    private User getAuthor() { // Make only accessible internally
        return author;
    }

    public UserId getAuthorId() {
        return author.getId();
    }

    public String getAuthorName() {
        return author.getName().get();
    }

    public String getAuthorEmail() {
        return author.getEmailAddress().get();
    }

    public Date getDate() {
        return date;
    }

    public String getCommitComment() {
        return comment;
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
        if (!(obj instanceof ChangeMetadata)) {
            return false;
        }
        ChangeMetadata other = (ChangeMetadata) obj;
        return other.getCommitComment().equals(comment) && other.getDate().equals(date)
                && other.getAuthor().equals(author);
    }

    @Override
    public String toString() {
        String template = "Committed by %s <%s> at %s: %s";
        return String.format(template, getAuthorId(), getAuthorEmail(), getDate(), getCommitComment());
    }
}
