package org.protege.editor.owl.server.http.handlers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.protege.editor.owl.server.api.ChangeService;
import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.OutOfSyncException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.change.ChangeManagementFilter;
import org.protege.editor.owl.server.change.DefaultChangeService;
import org.protege.editor.owl.server.conflict.ConflictDetectionFilter;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.policy.AccessControlFilter;
import org.protege.editor.owl.server.versioning.api.ChangeHistory;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;
import org.protege.editor.owl.server.versioning.api.HistoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.exception.PolicyException;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

public class HTTPChangeService extends BaseRoutingHandler {

	private static Logger logger = LoggerFactory.getLogger(HTTPChangeService.class);

	private ChangeService changeService;
	private ConflictDetectionFilter cf;
	private AccessControlFilter acf;

	public HTTPChangeService(ProtegeServer s) {
		cf = new ConflictDetectionFilter(new ChangeManagementFilter(s));
		acf = new AccessControlFilter(cf);
		changeService = new DefaultChangeService(acf.getChangePool());
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.COMMIT)) {
			/*
			 * Read client input
			 */
			ProjectId pid = null;
			CommitBundle bundle = null;
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				pid = (ProjectId) ois.readObject();
				bundle = (CommitBundle) ois.readObject();
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Failed to read incoming input paramters", e);
			}
			
			/*
			 * Processing the request
			 */
			ChangeHistory hist = null;
			try {
				hist = acf.commit(getAuthToken(exchange), pid, bundle);
			}
			catch (AuthorizationException e) {
				unauthorizedStatusCode(exchange, "Access denied", e);
			}
			catch (OutOfSyncException e) {
				conflictStatusCode(exchange, "Commit failed, please update your local copy first", e);
			}
			catch (ServerServiceException e) {
				if (e.getCause() instanceof PolicyException) {
					unauthorizedStatusCode(exchange, "Failed to get all changes due to insufficient access right", e);
				}
				else {
					internalServerErrorStatusCode(exchange, "Server failed to accept the commit", e);
				}
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			
			/*
			 * Send the return value
			 */
			try {
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(hist);
				exchange.endExchange();
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Failed to transmit the returned data", e);
			}
			
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.ALL_CHANGES)) {
			/*
			 * Read client input
			 */
			HistoryFile file = null;
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				file = (HistoryFile) ois.readObject();
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Failed to read incoming input paramters", e);
			}
			
			/*
			 * Processing the request
			 */
			ChangeHistory history = null;
			try {
				DocumentRevision headRevision = changeService.getHeadRevision(file);
				history = changeService.getChanges(file, DocumentRevision.START_REVISION, headRevision);
			}
			catch (ServerServiceException e) {
				if (e.getCause() instanceof PolicyException) {
					unauthorizedStatusCode(exchange, "Failed to get all changes due to insufficient access right", e);
				}
				else {
					internalServerErrorStatusCode(exchange, "Server failed to get all changes", e);
				}
			}
			
			/*
			 * Send the return value
			 */
			try {
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(history);
				exchange.endExchange();
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Failed to transmit the returned data", e);
			}
			
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.LATEST_CHANGES)) {
			/*
			 * Read client input
			 */
			HistoryFile file = null;
			DocumentRevision start = null;
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				file = (HistoryFile) ois.readObject();
				start = (DocumentRevision) ois.readObject();
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Failed to read incoming input paramters", e);
			}
			
			/*
			 * Processing the request
			 */
			ChangeHistory history = null;
			try {
				DocumentRevision headRevision = changeService.getHeadRevision(file);
				history = changeService.getChanges(file, start, headRevision);
			}
			catch (ServerServiceException e) {
				if (e.getCause() instanceof PolicyException) {
					unauthorizedStatusCode(exchange, "Failed to get the latest changes due to insufficient access right", e);
				}
				else {
					internalServerErrorStatusCode(exchange, "Server failed to get the latest changes", e);
				}
			}
			
			/*
			 * Send the return value
			 */
			try {
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(history);
				exchange.endExchange();
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Failed to transmit the returned data", e);
			}
			
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.HEAD)) {
			/*
			 * Read client input
			 */
			HistoryFile file = null;
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				file = (HistoryFile) ois.readObject();
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Failed to read incoming input paramters", e);
			}
			
			/*
			 * Processing the request
			 */
			DocumentRevision headRevision = null;
			try {
				//DocumentRevision start = (DocumentRevision) ois.readObject();
				headRevision = changeService.getHeadRevision(file);
			}
			catch (ServerServiceException e) {
				if (e.getCause() instanceof PolicyException) {
					unauthorizedStatusCode(exchange, "Failed to get the head revision due to insufficient access right", e);
				}
				else {
					internalServerErrorStatusCode(exchange, "Server failed to get the head revision", e);
				}
			}
			
			/*
			 * Send the return value
			 */
			try {
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(headRevision);
				exchange.endExchange();
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Failed to transmit the returned data", e);
			}
		}
	}

	private void conflictStatusCode(HttpServerExchange exchange, String message, Exception cause) {
		logger.error(cause.getMessage(), cause);
		exchange.setStatusCode(StatusCodes.CONFLICT);
		exchange.getResponseHeaders().add(new HttpString("Error-Message"), message);
		exchange.endExchange(); // end the request
	}

	private void internalServerErrorStatusCode(HttpServerExchange exchange, String message, Exception cause) {
		logger.error(cause.getMessage(), cause);
		exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
		exchange.getResponseHeaders().add(new HttpString("Error-Message"), message);
		exchange.endExchange(); // end the request
	}

	private void unauthorizedStatusCode(HttpServerExchange exchange, String message, Exception cause) {
		logger.error(cause.getMessage(), cause);
		exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
		exchange.getResponseHeaders().add(new HttpString("Error-Message"), message);
		exchange.endExchange(); // end the request
	}

	private void handleServerException(HttpServerExchange exchange, ServerException e) {
		exchange.setStatusCode(e.getErrorCode());
		exchange.getResponseHeaders().add(new HttpString("Error-Message"), e.getMessage());
		exchange.endExchange(); // end the request
	}
}
