package org.protege.editor.owl.server.http.handlers;

import com.google.inject.Inject;
import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.PolicyFactory;
import edu.stanford.protege.metaproject.api.Serializer;
import edu.stanford.protege.metaproject.api.exception.ObjectConversionException;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.protege.editor.owl.server.api.LoginService;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.http.messages.HttpAuthResponse;
import org.protege.editor.owl.server.http.messages.LoginCreds;
import org.protege.editor.owl.server.security.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.UUID;

import javax.annotation.Nonnull;

public class HTTPLoginService extends BaseRoutingHandler {
	
	private static Logger logger = LoggerFactory.getLogger(MetaprojectHandler.class);
	
	private final LoginService loginService;
	private final SessionManager sessionManager;
	
	@Inject
	public HTTPLoginService(@Nonnull LoginService loginService, @Nonnull SessionManager sessionManager) {
		this.loginService = loginService;
		this.sessionManager = sessionManager;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		Serializer serl = new DefaultJsonSerializer();
		try {
			PolicyFactory f = ConfigurationManager.getFactory();
			LoginCreds creds = (LoginCreds) serl.parse(new InputStreamReader(exchange.getInputStream()), LoginCreds.class);
			/*
			 * Use the login service over the wire
			 */
			AuthToken authToken = loginService.login(f.getUserId(creds.getUser()),
					f.getPlainPassword(creds.getPassword()));
			sendLoginResponse(authToken, exchange);
		}
		catch (ObjectConversionException e) {
			logger.error(e.getMessage(), e);
			exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
			exchange.getResponseHeaders().add(new HttpString("Error-Message"), "Server failed to read the login credential");
		}
		catch (ServerServiceException e) {
			logger.error(e.getMessage(), e);
			exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
			exchange.getResponseHeaders().add(new HttpString("Error-Message"), e.getMessage());
		}
		finally {
			exchange.endExchange(); // end the request
		}
	}

	private void sendLoginResponse(AuthToken authToken, HttpServerExchange exchange) {
		Serializer serl = new DefaultJsonSerializer();
		String tokenKey = UUID.randomUUID().toString();
		sessionManager.addSession(tokenKey, authToken);
		exchange.getResponseSender().send(serl.write(new HttpAuthResponse(tokenKey, authToken.getUser()), HttpAuthResponse.class));
	}
}

