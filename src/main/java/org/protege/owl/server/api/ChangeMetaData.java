package org.protege.owl.server.api;

import java.io.Serializable;
import java.util.Date;

public final class ChangeMetaData implements Serializable {
    private static final long serialVersionUID = -1198003999159038367L;
    private Date date;
    private String commitComment;
    private UserId userId = new UserId("");
    
    public ChangeMetaData(Date date, String commitComment) {
        this.date = date;
        this.commitComment = commitComment;
    }
    public ChangeMetaData(String commitComment) {
        this.date = new Date();
        this.commitComment = commitComment;
    }
    
    public ChangeMetaData() {
        this.date = new Date();
        this.commitComment = "";
    }
    
    public Date getDate() {
        return date;
    }
    public String getCommitComment() {
        return commitComment;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public void setUser(AuthToken user) {
        this.userId = user.getUserId();
    }

    @Override
    public int hashCode() {
        return date.hashCode() + 42 * commitComment.hashCode() + userId.hashCode()/42;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChangeMetaData)) {
            return false;
        }
        ChangeMetaData other = (ChangeMetaData) obj;
        return other.getCommitComment().equals(commitComment) && other.getDate().equals(date) && userId.equals(other.getUserId());
    }

    
    @Override
    public String toString() {
        return "Committed at " + date +": " + commitComment;
    }

}
