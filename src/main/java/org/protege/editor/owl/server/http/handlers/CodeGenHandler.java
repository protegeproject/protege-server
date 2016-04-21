package org.protege.editor.owl.server.http.handlers;

import org.protege.editor.owl.server.base.ProtegeServer;

import edu.stanford.protege.metaproject.impl.ProjectIdImpl;
import io.undertow.server.HttpServerExchange;

public class CodeGenHandler extends BaseRoutingHandler {
	
	private ProtegeServer server;
	
	public CodeGenHandler(ProtegeServer s) {
		server = s;		
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		
		String code = server.genCode(new ProjectIdImpl("top-project"));
		
		exchange.endExchange();
		// TODO Auto-generated method stub
		
	}

}
