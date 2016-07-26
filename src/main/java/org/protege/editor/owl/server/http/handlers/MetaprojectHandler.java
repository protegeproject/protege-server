package org.protege.editor.owl.server.http.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.change.ChangeManagementFilter;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.util.SnapShot;
import org.protege.editor.owl.server.versioning.api.ServerDocument;
import org.semanticweb.binaryowl.BinaryOWLOntologyDocumentSerializer;
import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyBuildingHandler;
import org.semanticweb.binaryowl.owlapi.OWLOntologyWrapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import edu.stanford.protege.metaproject.Manager;
import edu.stanford.protege.metaproject.api.Description;
import edu.stanford.protege.metaproject.api.MetaprojectAgent;
import edu.stanford.protege.metaproject.api.MetaprojectFactory;
import edu.stanford.protege.metaproject.api.Name;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectOptions;
import edu.stanford.protege.metaproject.api.Serializer;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.ObjectConversionException;
import edu.stanford.protege.metaproject.api.exception.ServerConfigurationNotLoadedException;
import edu.stanford.protege.metaproject.api.exception.UserNotInPolicyException;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

public class MetaprojectHandler extends BaseRoutingHandler {
	
	private static Logger logger = LoggerFactory.getLogger(MetaprojectHandler.class);

	private ServerConfiguration configuration;
	private ProtegeServer server;
	private ChangeManagementFilter cf;

	private MetaprojectAgent metaprojectAgent;

	private String configLocation;

	public MetaprojectHandler(ProtegeServer pserver) {
		server = pserver;
		cf = new ChangeManagementFilter(server);
		configuration = server.getConfiguration();
		metaprojectAgent = configuration.getMetaproject().getMetaprojectAgent();
		configLocation = System.getProperty(HTTPServer.SERVER_CONFIGURATION_PROPERTY);
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECTS)) {
			try {
				String uid = super.getQueryParameter(exchange, "userid");
				MetaprojectFactory f = Manager.getFactory();
				UserId userId = f.getUserId(uid);
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				List<Project> projects = new ArrayList<>(metaprojectAgent.getProjects(userId));
				os.writeObject(projects);
				exchange.endExchange(); // end the request
			}
			catch (UserNotInPolicyException e) {
				unauthorizedStatusCode(exchange, "Access denied", e);
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Failed to transmit the returned data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT) &&
				exchange.getRequestMethod().equals(Methods.POST)) {
			/*
			 * Receive input parameters
			 */
			ProjectId pid = null;
			Name pname = null;
			Description desc = null;
			UserId uid = null;
			Optional<ProjectOptions> oopts = null;
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				pid = (ProjectId) ois.readObject();
				pname = (Name) ois.readObject();
				desc = (Description) ois.readObject();
				uid = (UserId) ois.readObject();
				oopts = Optional.ofNullable((ProjectOptions) ois.readObject());
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Failed to read incoming input paramters", e);
			}
			
			/*
			 * Processing and send return value
			 */
			try {
				ServerDocument doc = cf.createProject(getAuthToken(exchange), pid, pname, desc, uid, oopts);
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(doc);
				exchange.endExchange(); // end the request
			}
			catch (AuthorizationException e) {
				unauthorizedStatusCode(exchange, "Access denied", e);
			}
			catch (ServerServiceException e) {
				internalServerErrorStatusCode(exchange, "Server failed to create the project", e);
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Failed to transmit the returned data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT)
				&& exchange.getRequestMethod().equals(Methods.GET)) {
			try {
				String pid = super.getQueryParameter(exchange, "projectid");
				MetaprojectFactory f = Manager.getFactory();
				ProjectId projId  = f.getProjectId(pid);
				ServerDocument sdoc = server.openProject(getAuthToken(exchange), projId);
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(sdoc);
				exchange.endExchange(); // end the request
			}
			catch (AuthorizationException e) {
				unauthorizedStatusCode(exchange, "Access denied", e);
			}
			catch (ServerServiceException e) {
				internalServerErrorStatusCode(exchange, "Server failed to open the project", e);
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Failed to transmit the returned data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT)
				&& exchange.getRequestMethod().equals(Methods.DELETE)) {
			try {
				String pid = super.getQueryParameter(exchange, "projectid");
				MetaprojectFactory f = Manager.getFactory();
				ProjectId projId  = f.getProjectId(pid);
				cf.deleteProject(getAuthToken(exchange), projId, true);
				exchange.endExchange(); // end the request
			}
			catch (AuthorizationException e) {
				unauthorizedStatusCode(exchange, "Access denied", e);
			}
			catch (ServerServiceException e) {
				internalServerErrorStatusCode(exchange, "Server failed to delete the project", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT_SNAPSHOT)
				&& exchange.getRequestMethod().equals(Methods.POST)) {
			/*
			 * Receive input parameters
			 */
			ServerDocument sdoc = null;
			SnapShot shot = null;
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				sdoc = (ServerDocument) ois.readObject();
				shot = (SnapShot) ois.readObject();
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Failed to read incoming input paramters", e);
			}

			/*
			 * Processing the request
			 */
			try {
				long beg = System.currentTimeMillis();
				String fileName = sdoc.getHistoryFile().getPath() + "-snapshot";
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
				BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
				serializer.write(new OWLOntologyWrapper(shot.getOntology()), new DataOutputStream(outputStream));
				outputStream.close();
				System.out.println("Time to serialize out snapshot " + (System.currentTimeMillis() - beg)/1000);
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Server failed to create project snapshot", e);
			}

			/*
			 * Send the return value
			 */
			try {
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(sdoc);
				exchange.endExchange();
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Failed to transmit the returned data", e);
			}

		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT_SNAPSHOT_GET)) {
			/*
			 * Receive input parameters
			 */
			ServerDocument sdoc = null;
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				sdoc = (ServerDocument) ois.readObject();
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Failed to read incoming input paramters", e);
			}
			
			/*
			 * Processing the request
			 */
			OWLOntology ontIn = null;
			try {
				long beg = System.currentTimeMillis();
				String fileName = sdoc.getHistoryFile().getPath() + "-snapshot";
				OWLOntologyManager manIn = OWLManager.createOWLOntologyManager();
				BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
				//OWLOntologyManager manIn = OWLManager.createOWLOntologyManager();
				ontIn = manIn.createOntology();
				BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(fileName)));
				serializer.read(inputStream, new BinaryOWLOntologyBuildingHandler(ontIn), manIn.getOWLDataFactory());
				System.out.println("Time to serialize in " + (System.currentTimeMillis() - beg)/1000);
			}
			catch (OWLOntologyCreationException | IOException e) {
				internalServerErrorStatusCode(exchange, "Server failed to fetch project snapshot", e);
			}
			
			/*
			 * Send the return value
			 */
			try {
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(new SnapShot(ontIn));
				exchange.endExchange();
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Failed to transmit the returned data", e);
			}
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.METAPROJECT) &&
				exchange.getRequestMethod().equals(Methods.GET)) {
			Serializer<Gson> serl = new DefaultJsonSerializer();
			exchange.getResponseSender().send(serl.write(configuration, ServerConfiguration.class));
			exchange.endExchange();
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.METAPROJECT) &&
				exchange.getRequestMethod().equals(Methods.POST)) {
			// TODO: After posting the new metaproject, we need to decide what to do with
			//       the config that's loaded in RAM
			ServerConfiguration cfg = null;
			try {
				Serializer<Gson> serl = new DefaultJsonSerializer();
				cfg = serl.parse(new InputStreamReader(exchange.getInputStream()), ServerConfiguration.class);
			}
			catch (FileNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Server failed to find the server configuration", e);
			}
			catch (ObjectConversionException e) {
				internalServerErrorStatusCode(exchange, "Server failed to load the server configuration", e);
			}
			
			try {
				Manager.getConfigurationManager().setServerConfiguration(cfg);
				Manager.getConfigurationManager().saveServerConfiguration(new File(configLocation));
				exchange.endExchange();
			}
			catch (ServerConfigurationNotLoadedException | IOException e) {
				internalServerErrorStatusCode(exchange, "Server failed to save changes", e);
			}
			
			try {
				HTTPServer.server().restart();
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
		}
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
