package org.protege.editor.owl.server.http.messages;

public class LoginCreds {
	
	private String user;
	private String password;
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}

	public LoginCreds(String user, String password) {
		this.user = user;
		this.password = password;
	}

}
