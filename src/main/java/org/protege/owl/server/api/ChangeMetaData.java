package org.protege.owl.server.api;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.Date;

public final class ChangeMetaData implements Serializable {
	private static final long serialVersionUID = -1198003999159038367L;
	private Date date;
	private String commitComment;
	private User user = new User("");
	
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
	
	public User getUser() {
        return user;
    }
	
	public void setUser(AuthToken user) {
        this.user = user.getUser();
    }

	@Override
	public int hashCode() {
		return Objects.hashCode(this.date, this.commitComment, this.user);
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (!(obj instanceof ChangeMetaData)) {
	        return false;
	    }
	    ChangeMetaData other = (ChangeMetaData) obj;
	    return other.getCommitComment().equals(commitComment) && other.getDate().equals(date) && user.equals(other.getUser());
	}

	
	@Override
	public String toString() {
	    return "Committed at " + date +": " + commitComment;
	}

}
