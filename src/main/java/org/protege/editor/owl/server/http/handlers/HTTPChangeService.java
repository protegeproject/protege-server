package org.protege.editor.owl.server.http.handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.http.ServerEndpoints;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.security.SessionManager;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.ProjectId;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

public class HTTPChangeService extends BaseRoutingHandler {

	private static final Logger logger = LoggerFactory.getLogger(HTTPChangeService.class);

	private final ServerLayer serverLayer;
	private final ChangeService changeService;
	private final SessionManager sessionManager;

	public HTTPChangeService(@Nonnull ServerLayer serverLayer, @Nonnull ChangeService changeService,
			@Nonnull SessionManager sessionManager) {
		this.serverLayer = serverLayer;
		this.changeService = changeService;
		this.sessionManager = sessionManager;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
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
		catch (ServerException e) {
			handleServerException(exchange, e);
		}
		finally {
			exchange.endExchange(); // end the request
		}
	}

	private void handlingRequest(AuthToken authToken, HttpServerExchange exchange) {
		String requestPath = exchange.getRequestPath();
		if (requestPath.equalsIgnoreCase(ServerEndpoints.COMMIT)) {
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				ProjectId pid = (ProjectId) ois.readObject();
				CommitBundle bundle = (CommitBundle) ois.readObject();
				submitCommitBundle(authToken, pid, bundle, exchange.getOutputStream());
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Server failed to receive the sent data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
		}
		else if (requestPath.equalsIgnoreCase(ServerEndpoints.ALL_CHANGES)) {
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				HistoryFile file = (HistoryFile) ois.readObject();
				retrieveAllChanges(file, exchange.getOutputStream());
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Server failed to receive the sent data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
		}
		else if (requestPath.equalsIgnoreCase(ServerEndpoints.LATEST_CHANGES)) {
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				HistoryFile file = (HistoryFile) ois.readObject();
				DocumentRevision start = (DocumentRevision) ois.readObject();
				retrieveLatestChanges(file, start, exchange.getOutputStream());
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Server failed to receive the sent data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
		}
		else if (requestPath.equalsIgnoreCase(ServerEndpoints.HEAD)) {
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				HistoryFile file = (HistoryFile) ois.readObject();
				retrieveHeadRevision(file, exchange.getOutputStream());
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Server failed to receive the sent data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
		}
	}

	private void submitCommitBundle(AuthToken authToken, ProjectId projectId, CommitBundle bundle,
			OutputStream os) throws ServerException {
		try {
			ChangeHistory hist = serverLayer.commit(authToken, projectId, bundle);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(hist);
		}
		catch (AuthorizationException e) {
			throw new ServerException(StatusCodes.UNAUTHORIZED, "Access denied", e);
		}
		catch (OutOfSyncException e) {
			throw new ServerException(StatusCodes.CONFLICT, "Commit failed, please update your local copy first", e);
		}
		catch (ServerServiceException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to receive the commit data", e);
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
		}
	}

	private void retrieveAllChanges(HistoryFile file, OutputStream os) throws ServerException {
		try {
			DocumentRevision headRevision = changeService.getHeadRevision(file);
			ChangeHistory history = changeService.getChanges(file, DocumentRevision.START_REVISION, headRevision);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(history);
		}
		catch (ServerServiceException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to get all changes", e);
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
		}
	}

	private void retrieveLatestChanges(HistoryFile file, DocumentRevision start, OutputStream os)
			throws ServerException {
		try {
			DocumentRevision headRevision = changeService.getHeadRevision(file);
			ChangeHistory history = changeService.getChanges(file, start, headRevision);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(history);
		}
		catch (ServerServiceException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to get the latest changes", e);
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
		}
	}

	private void retrieveHeadRevision(HistoryFile file, OutputStream os) throws ServerException {
		try {
			DocumentRevision headRevision = changeService.getHeadRevision(file);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(headRevision);
		}
		catch (ServerServiceException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to get the head revision", e);
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
		}
	}

	private void internalServerErrorStatusCode(HttpServerExchange exchange, String message, Exception cause) {
		logger.error(cause.getMessage(), cause);
		exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
		exchange.getResponseHeaders().add(new HttpString("Error-Message"), message);
	}

	private void handleServerException(HttpServerExchange exchange, ServerException e) {
		logger.error(e.getCause().getMessage(), e.getCause());
		exchange.setStatusCode(e.getErrorCode());
		exchange.getResponseHeaders().add(new HttpString("Error-Message"), e.getMessage());
	}
}
