package org.protege.editor.owl.server.http.handlers;

import com.google.gson.Gson;
import edu.stanford.protege.metaproject.Manager;
import edu.stanford.protege.metaproject.api.*;
import edu.stanford.protege.metaproject.api.exception.ObjectConversionException;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MetaprojectHandler extends BaseRoutingHandler {
	
	private static Logger logger = LoggerFactory.getLogger(MetaprojectHandler.class);

	private ConfigurationManager manager;
	private ServerConfiguration configuration;
	private ProtegeServer server;
	private ChangeManagementFilter cf;

	private String configLocation;

	public MetaprojectHandler(ProtegeServer pserver) {
		server = pserver;
		cf = new ChangeManagementFilter(server);
		configuration = server.getConfiguration();
		manager = configuration.getConfigurationManager();
		configLocation = System.getProperty(HTTPServer.SERVER_CONFIGURATION_PROPERTY);
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECTS)) {
			try {
				retrieveProjectList(exchange);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT) &&
				exchange.getRequestMethod().equals(Methods.POST)) {
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				ProjectId pid = (ProjectId) ois.readObject();
				Name pname = (Name) ois.readObject();
				Description desc = (Description) ois.readObject();
				UserId uid = (UserId) ois.readObject();
				Optional<ProjectOptions> oopts = Optional.ofNullable((ProjectOptions) ois.readObject());
				createNewProject(exchange, pid, pname, desc, uid, oopts);
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Server failed to receive the sent data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT)
				&& exchange.getRequestMethod().equals(Methods.GET)) {
			try {
				openExistingProject(exchange);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT)
				&& exchange.getRequestMethod().equals(Methods.DELETE)) {
			try {
				deleteExistingProject(exchange);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT_SNAPSHOT)
				&& exchange.getRequestMethod().equals(Methods.POST)) {
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				ServerDocument sdoc = (ServerDocument) ois.readObject();
				SnapShot shot = (SnapShot) ois.readObject();
				createProjectSnapshot(exchange, sdoc, shot);
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Server failed to receive the sent data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT_SNAPSHOT_GET)) {
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				ServerDocument sdoc = (ServerDocument) ois.readObject();
				retrieveProjectSnapshot(exchange, sdoc);
			}
			catch (IOException | ClassNotFoundException e) {
				internalServerErrorStatusCode(exchange, "Server failed to receive the sent data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.METAPROJECT) &&
				exchange.getRequestMethod().equals(Methods.GET)) {
			try {
				retrieveMetaproject(exchange);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.METAPROJECT) &&
				exchange.getRequestMethod().equals(Methods.POST)) {
			// TODO: After posting the new metaproject, we need to decide what to do with
			//       the config that's loaded in RAM
			try {
				Serializer<Gson> serl = new DefaultJsonSerializer();
				ServerConfiguration cfg = serl.parse(new InputStreamReader(exchange.getInputStream()), ServerConfiguration.class);
				updateMetaproject(cfg);
			}
			catch (ObjectConversionException e) {
				internalServerErrorStatusCode(exchange, "Server failed to receive the sent data", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
	}

	private void retrieveProjectList(HttpServerExchange exchange) throws ServerException {
		try {
			String uid = super.getQueryParameter(exchange, "userid");
			PolicyFactory f = Manager.getFactory();
			UserId userId = f.getUserId(uid);
			List<Project> projects = new ArrayList<>(configuration.getProjects(userId));
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
			os.writeObject(projects);
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
		}
	}

	private void createNewProject(HttpServerExchange exchange, ProjectId pid, Name pname,
			Description desc, UserId uid, Optional<ProjectOptions> oopts) throws ServerException {
		try {
			ServerDocument doc = cf.createProject(getAuthToken(exchange), pid, pname, desc, uid, oopts);
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
			os.writeObject(doc);
		}
		catch (AuthorizationException e) {
			throw new ServerException(StatusCodes.UNAUTHORIZED, "Access denied", e);
		}
		catch (ServerServiceException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to create the project", e);
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
		}
	}

	private void openExistingProject(HttpServerExchange exchange) throws ServerException {
		try {
			String pid = super.getQueryParameter(exchange, "projectid");
			PolicyFactory f = Manager.getFactory();
			ProjectId projId  = f.getProjectId(pid);
			ServerDocument sdoc = server.openProject(getAuthToken(exchange), projId);
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
			os.writeObject(sdoc);
		}
		catch (AuthorizationException e) {
			throw new ServerException(StatusCodes.UNAUTHORIZED, "Access denied", e);
		}
		catch (ServerServiceException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to open the project", e);
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
		}
	}

	private void deleteExistingProject(HttpServerExchange exchange) throws ServerException {
		try {
			String pid = super.getQueryParameter(exchange, "projectid");
			PolicyFactory f = Manager.getFactory();
			ProjectId projId  = f.getProjectId(pid);
			cf.deleteProject(getAuthToken(exchange), projId, true);
		}
		catch (AuthorizationException e) {
			throw new ServerException(StatusCodes.UNAUTHORIZED, "Access denied", e);
		}
		catch (ServerServiceException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to delete the project", e);
		}
	}

	private void createProjectSnapshot(HttpServerExchange exchange, ServerDocument sdoc,
			SnapShot shot) throws ServerException {
		try {
			long beg = System.currentTimeMillis();
			String fileName = sdoc.getHistoryFile().getPath() + "-snapshot";
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
			BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
			serializer.write(new OWLOntologyWrapper(shot.getOntology()), new DataOutputStream(outputStream));
			logger.info("Time to serialize out snapshot " + (System.currentTimeMillis() - beg)/1000);
			try {
				outputStream.close();
			}
			catch (IOException e) {
				// Ignore the exception but report it into the log
				logger.warn("Unable to close the file output stream used to serialize the snapshot");
			}
			try {
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(sdoc);
			}
			catch (IOException e) {
				throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
			}
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to create project snapshot", e);
		}
	}

	private void retrieveProjectSnapshot(HttpServerExchange exchange, ServerDocument sdoc) throws ServerException {
		OWLOntologyManager manIn = OWLManager.createOWLOntologyManager();
		try {
			long beg = System.currentTimeMillis();
			String fileName = sdoc.getHistoryFile().getPath() + "-snapshot";
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(fileName)));
			OWLOntology ontIn = manIn.createOntology(); // use as a placeholder
			BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
			serializer.read(inputStream, new BinaryOWLOntologyBuildingHandler(ontIn), manIn.getOWLDataFactory());
			System.out.println("Time to serialize in " + (System.currentTimeMillis() - beg)/1000);
			try {
				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(new SnapShot(ontIn));
			}
			catch (IOException e) {
				throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
			}
		}
		catch (OWLOntologyCreationException | IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to fetch project snapshot", e);
		}
	}

	private void retrieveMetaproject(HttpServerExchange exchange) throws ServerException {
		try {
			Serializer<Gson> serl = new DefaultJsonSerializer();
			exchange.getResponseSender().send(serl.write(configuration, ServerConfiguration.class));
		}
		catch (Exception e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to get server configuration", e);
		}
	}

	private void updateMetaproject(ServerConfiguration cfg) throws ServerException {
		try {
			manager.setActiveConfiguration(cfg);
			manager.saveConfiguration(new File(configLocation));
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to save changes of the metaproject", e);
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
