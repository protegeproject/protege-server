package org.protege.editor.owl.server.http.handlers;

import java.io.InputStreamReader;
import java.util.UUID;

import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.messages.HttpAuthResponse;
import org.protege.editor.owl.server.http.messages.LoginCreds;
import org.protege.editor.owl.server.security.DefaultLoginService;

import com.google.gson.Gson;
import com.google.inject.Inject;

import edu.stanford.protege.metaproject.Manager;
import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.MetaprojectFactory;
import edu.stanford.protege.metaproject.api.PlainPassword;
import edu.stanford.protege.metaproject.api.Salt;
import edu.stanford.protege.metaproject.api.SaltedPasswordDigest;
import edu.stanford.protege.metaproject.api.Serializer;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;


public class HTTPLoginService extends BaseRoutingHandler {
	
	private DefaultLoginService loginService;
	
	@Inject
	public HTTPLoginService(DefaultLoginService s) {
		super();
		this.loginService = s;
	}

	@Override
	public void handleRequest(final HttpServerExchange theExchange) {

		Serializer<Gson> serl = new DefaultJsonSerializer();		

		LoginCreds creds = null;
		
		try {
			creds = (LoginCreds) serl.parse(new InputStreamReader(theExchange.getInputStream()), LoginCreds.class);


			MetaprojectFactory f = Manager.getFactory();

			UserId userId = f.getUserId(creds.getUser());
			PlainPassword plainPassword = f.getPlainPassword(creds.getPassword());		
			Salt userSalt;

			userSalt = (Salt) loginService.getSalt(userId);

			SaltedPasswordDigest passwordDigest = f.getPasswordHasher().hash(plainPassword, userSalt);

			AuthToken authToken;

			authToken = loginService.login(userId, passwordDigest);

			
			String key = UUID.randomUUID().toString();

			HTTPServer.server().addSession(key, authToken);

			theExchange.getResponseSender().send(serl.write(new HttpAuthResponse(key, authToken.getUser()), HttpAuthResponse.class));
		} catch (ServerServiceException e) {
			theExchange.setStatusCode(StatusCodes.UNAUTHORIZED);
			theExchange.getResponseSender().send(serl.write(e.getMessage(), String.class));
			
		} catch (Exception e) {
			theExchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);

		}
		theExchange.endExchange();
	}

}

