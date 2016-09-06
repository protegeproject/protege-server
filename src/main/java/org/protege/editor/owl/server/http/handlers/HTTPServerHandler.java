package org.protege.editor.owl.server.http.handlers;

import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.ServerEndpoints;
import org.protege.editor.owl.server.http.exception.ServerException;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

public class HTTPServerHandler extends BaseRoutingHandler {

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		try {
			handlingRequest(exchange);
		}
		catch (ServerException e) {
			handleServerException(exchange, e);
		}
		finally {
			exchange.endExchange(); // end the request
		}
	}

	private void handlingRequest(HttpServerExchange exchange) throws ServerException {
		String requestPath = exchange.getRequestPath();
		HttpString requestMethod = exchange.getRequestMethod();
		if (requestPath.equals(ServerEndpoints.SERVER_RESTART) && requestMethod.equals(Methods.POST)) {
			HTTPServer.server().restart();
		}
		else if (requestPath.equals(ServerEndpoints.SERVER_STOP) && requestMethod.equals(Methods.POST)) {
			HTTPServer.server().stop();
		}
	}
}
