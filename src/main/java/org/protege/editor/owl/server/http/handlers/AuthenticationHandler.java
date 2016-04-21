package org.protege.editor.owl.server.http.handlers;

import org.apache.commons.codec.binary.Base64;
import org.protege.editor.owl.server.http.HTTPServer;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;


public final class AuthenticationHandler extends BaseRoutingHandler {

    private volatile HttpHandler handler;

    public AuthenticationHandler(final HttpHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
    	

		String fauth = getHeaderValue(exchange, Headers.AUTHORIZATION, "none");
				
		String coded = fauth.substring(fauth.indexOf(" ") + 1);
		String decAuth = new String(Base64.decodeBase64(coded));
		
		String userid = decAuth.substring(0,decAuth.indexOf(":"));
		String token = decAuth.substring(decAuth.indexOf(":") + 1);
				
		if (HTTPServer.server().validateToken(userid, token)) {
			handler.handleRequest(exchange);			
        } else {
        	exchange.endExchange();          	
        }
    }

    public HttpHandler getHandler() {
        return handler;
    }
}
