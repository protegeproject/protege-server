package org.protege.editor.owl.server.http.messages;

public class EVSHistory {
	private String code;
	private String name;
	private String operation;
	private String reference;
	
	public EVSHistory(String c, String n, String op, String ref) {
		code = c;
		name = n;
		operation = op;
		reference = ref;
	}
	
	public String toRecord() {
		return code + "\t" +
			name + "\t" +
			operation + "\t" +
			reference;
				
	}

}
