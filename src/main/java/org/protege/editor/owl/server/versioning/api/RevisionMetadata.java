package org.protege.editor.owl.server.versioning.api;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nonnull;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public final class RevisionMetadata implements Serializable {

    private static final long serialVersionUID = -1198003999159038367L;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private String authorName;
    private String authorId;
    private String authorEmail;

    private Date changeDate;
    private String comment;

    public RevisionMetadata(@Nonnull String authorId, String authorName, String authorEmail, @Nonnull String comment) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.changeDate = new Date();
        this.comment = comment;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public Date getDate() {
        return changeDate;
    }

    public String getComment() {
        return comment;
    }

    public String getLogMessage() {
        String authorFormat = String.format("%s", getAuthorId());
        if (authorName != null || !authorName.isEmpty()) {
            authorFormat = String.format("%s (%s)", getAuthorName(), getAuthorId());
            if (authorEmail != null || !authorEmail.isEmpty()) {
                authorFormat = String.format("%s (%s) <%s>", getAuthorName(), getAuthorId(), getAuthorEmail());
            }
        }
        else {
            if (authorEmail != null || !authorEmail.isEmpty()) {
                authorFormat = String.format("%s <%s>", getAuthorId(), getAuthorEmail());
            }
        }
        String template = "Author: %s\n"
                + "Date: %s\n"
                + "Comment: %s\n";
        return String.format(template, authorFormat, dateFormat.format(getDate()), getComment());
    }

    @Override
    public int hashCode() {
        return authorId.hashCode() + 42 * comment.hashCode() + changeDate.hashCode() / 42;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RevisionMetadata)) {
            return false;
        }
        RevisionMetadata other = (RevisionMetadata) obj;
        return other.getAuthorId().equals(authorId) && other.getDate().equals(changeDate)
                && other.getComment().equals(comment);
    }

    @Override
    public String toString() {
        return getLogMessage();
    }
}
