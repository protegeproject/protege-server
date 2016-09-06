package org.protege.editor.owl.server.http.handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.http.ServerEndpoints;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.security.LoginTimeoutException;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;

import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.ProjectId;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

public class HTTPChangeService extends BaseRoutingHandler {

	private final ServerLayer serverLayer;
	private final ChangeService changeService;

	public HTTPChangeService(ServerLayer serverLayer, ChangeService changeService) {
		this.serverLayer = serverLayer;
		this.changeService = changeService;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		try {
			handlingRequest(exchange);
		}
		catch (IOException | ClassNotFoundException e) {
			internalServerErrorStatusCode(exchange, "Server failed to receive the sent data", e);
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

	private void handlingRequest(HttpServerExchange exchange)
			throws IOException, ClassNotFoundException, LoginTimeoutException, ServerException {
		String requestPath = exchange.getRequestPath();
		if (requestPath.equalsIgnoreCase(ServerEndpoints.COMMIT)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			ProjectId pid = (ProjectId) ois.readObject();
			CommitBundle bundle = (CommitBundle) ois.readObject();
			submitCommitBundle(getAuthToken(exchange), pid, bundle, exchange.getOutputStream());
		}
		else if (requestPath.equalsIgnoreCase(ServerEndpoints.ALL_CHANGES)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			HistoryFile file = (HistoryFile) ois.readObject();
			retrieveAllChanges(file, exchange.getOutputStream());
		}
		else if (requestPath.equalsIgnoreCase(ServerEndpoints.LATEST_CHANGES)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			HistoryFile file = (HistoryFile) ois.readObject();
			DocumentRevision start = (DocumentRevision) ois.readObject();
			retrieveLatestChanges(file, start, exchange.getOutputStream());
		}
		else if (requestPath.equalsIgnoreCase(ServerEndpoints.HEAD)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			HistoryFile file = (HistoryFile) ois.readObject();
			retrieveHeadRevision(file, exchange.getOutputStream());
		}
	}

	/*
	 * Private methods that handlers each service provided by the server end-point above.
	 */

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
}
