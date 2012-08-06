package org.protege.owl.server.api;

import java.io.Serializable;
import java.util.Date;

public class ChangeMetaData implements Serializable {
	private static final long serialVersionUID = -1198003999159038367L;
	private Date date;
	private String commitComment;
	
	public ChangeMetaData(Date date, String commitComment) {
		this.date = date;
		this.commitComment = commitComment;
	}
	public ChangeMetaData(String commitComment) {
		this.date = new Date();
		this.commitComment = commitComment;
	}
	public Date getDate() {
		return date;
	}
	public String getCommitComment() {
		return commitComment;
	}
	
//	

}
