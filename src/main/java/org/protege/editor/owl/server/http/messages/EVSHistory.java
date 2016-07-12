package org.protege.editor.owl.server.http.messages;

import java.io.Serializable;

public class EVSHistory implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4521102352676041770L;
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
