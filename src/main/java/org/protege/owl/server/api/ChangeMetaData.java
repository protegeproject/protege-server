package org.protege.owl.server.api;

import java.io.Serializable;
import java.util.Date;

public class ChangeMetaData implements Serializable {
	private static final long serialVersionUID = -1198003999159038367L;
	private Date date;
	private String commitComment;
	private String username = "";
	
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
	
	public String getUsername() {
        return username;
    }
	
	public void setUser(User user) {
        this.username = user.getUserName();
    }

	@Override
	public int hashCode() {
	    return date.hashCode() + 42 * commitComment.hashCode() + username.hashCode()/42;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (!(obj instanceof ChangeMetaData)) {
	        return false;
	    }
	    ChangeMetaData other = (ChangeMetaData) obj;
	    return other.getCommitComment().equals(commitComment) && other.getDate().equals(date) && username.equals(other.getUsername());
	}

	
	@Override
	public String toString() {
	    return "Committed at " + date +": " + commitComment;
	}

}
