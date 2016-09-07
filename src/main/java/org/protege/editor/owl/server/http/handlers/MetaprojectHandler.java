package org.protege.editor.owl.server.http.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.protege.editor.owl.server.api.ServerLayer;
import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.api.exception.ServerServiceException;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.http.ServerEndpoints;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.security.LoginTimeoutException;
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

import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.Description;
import edu.stanford.protege.metaproject.api.Name;
import edu.stanford.protege.metaproject.api.PolicyFactory;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectOptions;
import edu.stanford.protege.metaproject.api.Serializer;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.ObjectConversionException;
import edu.stanford.protege.metaproject.api.exception.UnknownProjectIdException;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

public class MetaprojectHandler extends BaseRoutingHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(MetaprojectHandler.class);
	private static final PolicyFactory f = ConfigurationManager.getFactory();

	private final ServerLayer serverLayer;

	public MetaprojectHandler(ServerLayer serverLayer) {
		this.serverLayer = serverLayer;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		try {
			handlingRequest(exchange);
		}
		catch (IOException | ClassNotFoundException | ObjectConversionException e) {
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
			throws IOException, ClassNotFoundException, ObjectConversionException,
			LoginTimeoutException, ServerException {
		String requestPath = exchange.getRequestPath();
		HttpString requestMethod = exchange.getRequestMethod();
		if (requestPath.equals(ServerEndpoints.PROJECTS)) {
			UserId userId = f.getUserId(getQueryParameter(exchange, "userid"));
			retrieveProjectList(userId, exchange.getOutputStream());
		}
		else if (requestPath.equals(ServerEndpoints.PROJECT) && requestMethod.equals(Methods.POST)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			ProjectId pid = (ProjectId) ois.readObject();
			Name pname = (Name) ois.readObject();
			Description desc = (Description) ois.readObject();
			UserId uid = (UserId) ois.readObject();
			Optional<ProjectOptions> oopts = Optional.ofNullable((ProjectOptions) ois.readObject());
			createNewProject(getAuthToken(exchange), pid, pname, desc, uid, oopts, exchange.getOutputStream());
		}
		else if (requestPath.equals(ServerEndpoints.PROJECT) && requestMethod.equals(Methods.GET)) {
			ProjectId projectId = f.getProjectId(getQueryParameter(exchange, "projectid"));
			openExistingProject(getAuthToken(exchange), projectId, exchange.getOutputStream());
		}
		else if (requestPath.equals(ServerEndpoints.PROJECT) && requestMethod.equals(Methods.DELETE)) {
			ProjectId projectId = f.getProjectId(getQueryParameter(exchange, "projectid"));
			deleteExistingProject(getAuthToken(exchange), projectId);
		}
		else if (requestPath.equals(ServerEndpoints.PROJECT_SNAPSHOT) && requestMethod.equals(Methods.POST)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			ProjectId pid = (ProjectId) ois.readObject();
			SnapShot snapshot = (SnapShot) ois.readObject();
			createProjectSnapshot(pid, snapshot, exchange.getOutputStream());
		}
		else if (requestPath.equals(ServerEndpoints.PROJECT_SNAPSHOT) && requestMethod.equals(Methods.GET)) {
			ProjectId projectId = f.getProjectId(getQueryParameter(exchange, "projectid"));
			retrieveProjectSnapshot(projectId, exchange.getOutputStream());
		}
		else if (requestPath.equals(ServerEndpoints.METAPROJECT) && requestMethod.equals(Methods.GET)) {
			retrieveMetaproject(exchange);
		}
		else if (requestPath.equals(ServerEndpoints.METAPROJECT) && requestMethod.equals(Methods.POST)) {
			Serializer serl = new DefaultJsonSerializer();
			ServerConfiguration cfg = serl.parse(new InputStreamReader(exchange.getInputStream()), ServerConfiguration.class);
			updateMetaproject(cfg);
			HTTPServer.server().restart();
		}
	}

	/*
	 * Private methods that handlers each service provided by the server end-point above.
	 */

	private void retrieveProjectList(UserId userId, OutputStream os) throws ServerException {
		try {
			List<Project> projects = new ArrayList<>(serverLayer.getConfiguration().getProjects(userId));
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(projects);
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
		}
	}

	private void createNewProject(AuthToken authToken, ProjectId pid, Name pname,
			Description desc, UserId uid, Optional<ProjectOptions> oopts, OutputStream os) throws ServerException {
		try {
			ServerDocument doc = serverLayer.createProject(authToken, pid, pname, desc, uid, oopts);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(doc);
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

	private void openExistingProject(AuthToken authToken, ProjectId projectId, OutputStream os) throws ServerException {
		try {
			ServerDocument sdoc = serverLayer.openProject(authToken, projectId);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(sdoc);
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

	private void deleteExistingProject(AuthToken authToken, ProjectId projectId) throws ServerException {
		try {
			serverLayer.deleteProject(authToken, projectId, true);
		}
		catch (AuthorizationException e) {
			throw new ServerException(StatusCodes.UNAUTHORIZED, "Access denied", e);
		}
		catch (ServerServiceException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to delete the project", e);
		}
	}

	private void createProjectSnapshot(ProjectId projectId, SnapShot snapshot, OutputStream os) throws ServerException {
		try {
			Project project = serverLayer.getConfiguration().getProject(projectId);
			File projectFile = project.getFile();
			saveProjectSnapshot(snapshot, getSnapShotFile(projectFile));
		}
		catch (IOException | UnknownProjectIdException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to create project snapshot", e);
		}
	}

	private void saveProjectSnapshot(SnapShot snapshot, File snapshotFile) throws IOException {
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(snapshotFile));
		try {
			BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
			long start = System.currentTimeMillis();
			serializer.write(new OWLOntologyWrapper(snapshot.getOntology()), new DataOutputStream(outputStream));
			logger.info("Saving snapshot in " + (System.currentTimeMillis() - start) + " ms");
		}
		finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				}
				catch (IOException e) {
					// Ignore the exception but report it into the log
					logger.warn("Unable to close the file output stream used to save the snapshot", e);
				}
			}
		}
	}

	private void retrieveProjectSnapshot(ProjectId projectId, OutputStream os) throws ServerException {
		try {
			Project project = serverLayer.getConfiguration().getProject(projectId);
			File projectFile = project.getFile();
			OWLOntology ontIn = loadProjectSnapshot(getSnapShotFile(projectFile));
			try {
				ObjectOutputStream oos = new ObjectOutputStream(os);
				oos.writeObject(new SnapShot(ontIn));
			}
			catch (IOException e) {
				throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to transmit the returned data", e);
			}
		}
		catch (OWLOntologyCreationException | IOException | UnknownProjectIdException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to fetch project snapshot", e);
		}
	}

	private static File getSnapShotFile(File historyFile) {
		String fname = historyFile.getAbsolutePath() + "-snapshot";
		return new File(fname);
	}

	private OWLOntology loadProjectSnapshot(File snapshotFile) throws OWLOntologyCreationException, IOException {
		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(snapshotFile));
		try {
			OWLOntologyManager ontoManager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = ontoManager.createOntology(); // use as a placeholder
			BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
			long start = System.currentTimeMillis();
			serializer.read(inputStream,
					new BinaryOWLOntologyBuildingHandler(ontology),
					ontoManager.getOWLDataFactory());
			System.out.println("Reading snapshot in " + (System.currentTimeMillis() - start) + " ms");
			return ontology;
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				}
				catch (IOException e) {
					// Ignore the exception but report it into the log
					logger.warn("Unable to close the file input stream used to load the snapshot", e);
				}
			}
		}
	}

	private void retrieveMetaproject(HttpServerExchange exchange) throws ServerException {
		try {
			Serializer serl = new DefaultJsonSerializer();
			exchange.getResponseSender().send(serl.write(serverLayer.getConfiguration(), ServerConfiguration.class));
		}
		catch (Exception e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to get server configuration", e);
		}
	}

	private void updateMetaproject(ServerConfiguration cfg) throws ServerException {
		try {
			String configLocation = System.getProperty(HTTPServer.SERVER_CONFIGURATION_PROPERTY);
			ConfigurationManager.getConfigurationWriter().saveConfiguration(cfg, new File(configLocation));
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to save changes of the metaproject", e);
		}
	}
}
