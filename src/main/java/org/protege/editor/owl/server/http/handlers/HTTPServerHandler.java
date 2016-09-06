package org.protege.editor.owl.server.http.handlers;

import edu.stanford.protege.metaproject.api.AuthToken;

import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.ServerEndpoints;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.security.LoginTimeoutException;
import org.protege.editor.owl.server.security.SessionManager;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

import javax.annotation.Nonnull;

public class HTTPServerHandler extends BaseRoutingHandler {

	private final HTTPServer httpServer;
	private final SessionManager sessionManager;

	public HTTPServerHandler(@Nonnull HTTPServer httpServer, @Nonnull SessionManager sessionManager) {
		this.httpServer = httpServer;
		this.sessionManager = sessionManager;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		try {
			String tokenKey = getTokenKey(exchange);
			AuthToken authToken = sessionManager.getAuthToken(tokenKey);
			if (sessionManager.validate(authToken, getTokenOwner(exchange))) {
				handlingRequest(authToken, exchange);
			}
			else {
				exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
				exchange.getResponseHeaders().add(new HttpString("Error-Message"), "Access denied");
			}
		}
		catch (LoginTimeoutException e) {
			loginTimeoutErrorStatusCode(exchange, e);
		}
		catch (ServerException e) {
			handleServerException(exchange, e);
		}
		finally {
			exchange.endExchange(); // end the request
		}
	}

	private void handlingRequest(AuthToken authToken, HttpServerExchange exchange) throws ServerException {
		String requestPath = exchange.getRequestPath();
		if (requestPath.equalsIgnoreCase(ServerEndpoints.SERVER_RESTART) && exchange.getRequestMethod().equals(Methods.POST)) {
			httpServer.restart();
		}
		else if (requestPath.equalsIgnoreCase(ServerEndpoints.SERVER_STOP) && exchange.getRequestMethod().equals(Methods.POST)) {
			httpServer.stop();
		}
	}
}
