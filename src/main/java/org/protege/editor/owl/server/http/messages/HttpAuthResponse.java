package org.protege.editor.owl.server.http.messages;

import edu.stanford.protege.metaproject.api.User;

public class HttpAuthResponse {
	private String token;
	
	private String userid;
	
	private String user_name;
	
	private String user_email;
	
	public String getId() {
		return userid;
	}
	
	public String getName() {
		return user_name;
	}
	
	public String getEmail() {
		return user_email;
	}
	
	public String getToken() {
		return token;
	}
	
	public HttpAuthResponse(String toke, User u) {
		token = toke;
		userid = u.getId().get();
		user_name = u.getName().get();
		user_email = u.getEmailAddress().get();
	}
	
	

}
