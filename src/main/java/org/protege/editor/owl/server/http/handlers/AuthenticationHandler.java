package org.protege.editor.owl.server.http.handlers;

import org.apache.commons.codec.binary.Base64;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.security.LoginTimeoutException;

import edu.stanford.protege.metaproject.api.AuthToken;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

public final class AuthenticationHandler extends BaseRoutingHandler {

	private volatile HttpHandler handler;

	public AuthenticationHandler(final HttpHandler handler) {
		this.handler = handler;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		String authHeaderMessage = getHeaderValue(exchange, Headers.AUTHORIZATION, "none");
		String authBlockCode = authHeaderMessage.substring(authHeaderMessage.indexOf(" ") + 1);
		String authString = new String(Base64.decodeBase64(authBlockCode));
		String userId = authString.substring(0, authString.indexOf(":"));
		String tokenString = authString.substring(authString.indexOf(":") + 1);
		try {
			AuthToken authToken = HTTPServer.server().getAuthToken(tokenString);
			if (authToken != null && authToken.isAuthorized()) {
				if (isValid(userId, authToken)) {
					handler.handleRequest(exchange);
				}
				else {
					throw new ServerException(StatusCodes.UNAUTHORIZED, "Invalid user token");
				}
			}
			else {
				throw new ServerException(StatusCodes.UNAUTHORIZED, "Access denied");
			}
		}
		catch (LoginTimeoutException e) {
			loginTimeoutErrorStatusCode(exchange, e);
			exchange.endExchange();
		}
		catch (ServerException e) {
			handleServerException(exchange, e);
			exchange.endExchange();
		}
	}

	private boolean isValid(String userid, AuthToken authToken) {
		return userid.equalsIgnoreCase(authToken.getUser().getId().get());
	}

	public HttpHandler getHandler() {
		return handler;
	}
}
