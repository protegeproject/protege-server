package org.protege.editor.owl.server.http.handlers;

import java.io.InputStreamReader;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.protege.editor.owl.server.api.LoginService;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.http.messages.HttpAuthResponse;
import org.protege.editor.owl.server.http.messages.LoginCreds;

import com.google.inject.Inject;

import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.PlainPassword;
import edu.stanford.protege.metaproject.api.PolicyFactory;
import edu.stanford.protege.metaproject.api.Serializer;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.ObjectConversionException;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

public class HTTPLoginService extends BaseRoutingHandler {

	private final LoginService loginService;

	@Inject
	public HTTPLoginService(@Nonnull LoginService loginService) {
		this.loginService = loginService;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) {
		Serializer serl = new DefaultJsonSerializer();
		try {
			LoginCreds creds = (LoginCreds) serl.parse(new InputStreamReader(exchange.getInputStream()), LoginCreds.class);
			AuthToken authToken = login(creds);
			sendLoginResponse(exchange, authToken);
		}
		catch (ObjectConversionException e) {
			internalServerErrorStatusCode(exchange, "Server failed to read the login credential", e);
		}
		catch (ServerException e) {
			handleServerException(exchange, e);
		}
		finally {
			exchange.endExchange(); // end the request
		}
	}

	private AuthToken login(LoginCreds credential) throws ServerException {
		try {
			/*
			 * Use the login service over the wire
			 */
			PolicyFactory f = ConfigurationManager.getFactory();
			UserId userId = f.getUserId(credential.getUser());
			PlainPassword plainPassword = f.getPlainPassword(credential.getPassword());
			AuthToken authToken = loginService.login(userId, plainPassword);
			return authToken;
		}
		catch (ServerServiceException e) {
			throw new ServerException(StatusCodes.UNAUTHORIZED, "Invalid combination of username and password", e);
		}
	}

	private void sendLoginResponse(final HttpServerExchange exchange, AuthToken authToken) {
		Serializer serl = new DefaultJsonSerializer();
		String key = UUID.randomUUID().toString();
		HTTPServer.server().addSession(key, authToken);
		exchange.getResponseSender().send(serl.write(new HttpAuthResponse(key, authToken.getUser()), HttpAuthResponse.class));
	}
}

