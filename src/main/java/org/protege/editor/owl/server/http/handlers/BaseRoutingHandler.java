package org.protege.editor.owl.server.http.handlers;

import java.util.Deque;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.security.LoginTimeoutException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Strings;

import edu.stanford.protege.metaproject.api.AuthToken;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

public abstract class BaseRoutingHandler implements HttpHandler {

	protected final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	public BaseRoutingHandler() {
		// NO-OP
	}

	protected String getHeaderValue(final HttpServerExchange theExchange, final HttpString theAttr,
			final String theDefault) {
		HeaderValues aVals = theExchange.getRequestHeaders().get(theAttr);
		return !aVals.isEmpty() ? aVals.getFirst() : theDefault;
	}

	protected String getQueryParameter(final HttpServerExchange theExchange, final String paramName)
			throws ServerException {
		final Map<String, Deque<String>> queryParams = theExchange.getQueryParameters();
		if (!queryParams.containsKey(paramName) || queryParams.get(paramName).isEmpty()) {
			throwBadRequest("Missing required parameter: " + paramName);
		}
		final String paramVal = queryParams.get(paramName).getFirst();
		if (Strings.isNullOrEmpty(paramVal)) {
			throwBadRequest("Missing required parameter: " + paramName);
		}
		return paramVal;
	}

	protected AuthToken getAuthToken(final HttpServerExchange ex) throws LoginTimeoutException {
		String fauth = getHeaderValue(ex, Headers.AUTHORIZATION, "none");
		String coded = fauth.substring(fauth.indexOf(" ") + 1);
		String decAuth = new String(Base64.decodeBase64(coded));
		String token = decAuth.substring(decAuth.indexOf(":") + 1);
		return HTTPServer.server().getAuthToken(token);
	}

	protected ServerException throwBadRequest(final String theMsg) throws ServerException {
		throw new ServerException(StatusCodes.BAD_REQUEST, theMsg);
	}
}
