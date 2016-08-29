package org.protege.editor.owl.server.http.handlers;

import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.ServerEndpoints;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

public class HTTPServerHandler extends BaseRoutingHandler {

	private static Logger logger = LoggerFactory.getLogger(HTTPServerHandler.class);

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.getRequestPath().equalsIgnoreCase(ServerEndpoints.SERVER_RESTART) &&
				exchange.getRequestMethod().equals(Methods.POST)) {
			try {
				HTTPServer.server().restart();
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(ServerEndpoints.SERVER_STOP) &&
				exchange.getRequestMethod().equals(Methods.POST)) {
			try {
				HTTPServer.server().stop();
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
	}

	private void handleServerException(HttpServerExchange exchange, ServerException e) {
		logger.error(e.getCause().getMessage(), e.getCause());
		exchange.setStatusCode(e.getErrorCode());
		exchange.getResponseHeaders().add(new HttpString("Error-Message"), e.getMessage());
	}
}
