package org.protege.editor.owl.server.http.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.change.ChangeManagementFilter;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.util.SnapShot;
import org.protege.editor.owl.server.versioning.api.ServerDocument;
import org.semanticweb.binaryowl.BinaryOWLOntologyDocumentSerializer;
import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyBuildingHandler;
import org.semanticweb.binaryowl.owlapi.OWLOntologyWrapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

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
import edu.stanford.protege.metaproject.api.exception.UserNotInPolicyException;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

public class MetaprojectHandler extends BaseRoutingHandler {
	
	private ServerConfiguration configuration;
	private ProtegeServer server;
	private ChangeManagementFilter cf;

    private MetaprojectAgent metaprojectAgent;
    
    private File cfgFile;
    
    public MetaprojectHandler(ProtegeServer pserver) {
    	server = pserver;
    	cf = new ChangeManagementFilter(server);
    	configuration = server.getConfiguration();        
        metaprojectAgent = configuration.getMetaproject().getMetaprojectAgent();
        
        String configLocation = System.getProperty(HTTPServer.SERVER_CONFIGURATION_PROPERTY);
        cfgFile = new File(configLocation);
    }

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECTS)) {

			String uid = super.getQueryParameter(exchange, "userid");

			MetaprojectFactory f = Manager.getFactory();

			UserId userId = f.getUserId(uid);
			
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());

			try {
				List<Project> projects = new ArrayList<>(metaprojectAgent.getProjects(userId));
				        
				os.writeObject(projects);
				
			} catch (UserNotInPolicyException ex) {
				exchange.setStatusCode(StatusCodes.UNAUTHORIZED);
				
			}
			exchange.endExchange();
			
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT) &&
				exchange.getRequestMethod().equals(Methods.POST)) {
			
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			ProjectId pid = (ProjectId) ois.readObject();
			Name pname = (Name) ois.readObject();
			Description desc = (Description) ois.readObject();
			UserId uid = (UserId) ois.readObject();
			ProjectOptions opts = (ProjectOptions) ois.readObject();
			Optional<ProjectOptions> oopts = Optional.ofNullable(opts);		
			
			ServerDocument doc = cf.createProject(getAuthToken(exchange), pid, pname, desc, uid, oopts);			
			
	        ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
	        
	        os.writeObject(doc);
			exchange.endExchange();
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT)
				&& exchange.getRequestMethod().equals(Methods.GET)) {
			String pid = super.getQueryParameter(exchange, "projectid");

			MetaprojectFactory f = Manager.getFactory();

			ProjectId projId  = f.getProjectId(pid);
			ServerDocument sdoc = server.openProject(getAuthToken(exchange), projId);
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
	        
	        os.writeObject(sdoc);
			exchange.endExchange();
			
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT)
				&& exchange.getRequestMethod().equals(Methods.DELETE)) {
			String pid = super.getQueryParameter(exchange, "projectid");

			MetaprojectFactory f = Manager.getFactory();

			ProjectId projId  = f.getProjectId(pid);
			cf.deleteProject(getAuthToken(exchange), projId, true);
			
			exchange.endExchange();
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT_SNAPSHOT)
				&& exchange.getRequestMethod().equals(Methods.POST)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			ServerDocument sdoc = (ServerDocument) ois.readObject();

			SnapShot shot = (SnapShot) ois.readObject();


			try {

				long beg = System.currentTimeMillis();
				BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
				BufferedOutputStream outputStream = null;

				String fileName = sdoc.getHistoryFile().getPath() + "-snapshot";

				outputStream = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
				serializer.write(new OWLOntologyWrapper(shot.getOntology()), new DataOutputStream(outputStream));
				outputStream.close();
				System.out.println("Time to serialize out snapshot " + (System.currentTimeMillis() - beg)/1000);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}     



			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());

			os.writeObject(sdoc);		

			exchange.endExchange();

		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT_SNAPSHOT_GET)) {
			OWLOntology ontIn = null;
			
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				ServerDocument sdoc = (ServerDocument) ois.readObject();
				String fileName = sdoc.getHistoryFile().getPath() + "-snapshot";
				
				OWLOntologyManager manIn = OWLManager.createOWLOntologyManager();
				long beg = System.currentTimeMillis();
				BinaryOWLOntologyDocumentSerializer serializer = new BinaryOWLOntologyDocumentSerializer();
				//OWLOntologyManager manIn = OWLManager.createOWLOntologyManager();
		        ontIn = manIn.createOntology();
		        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(fileName)));
		        serializer.read(inputStream, new BinaryOWLOntologyBuildingHandler(ontIn), manIn.getOWLDataFactory());
		        System.out.println("Time to serialize in " + (System.currentTimeMillis() - beg)/1000);
		        
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
	        
	        os.writeObject(new SnapShot(ontIn));		
			
			exchange.endExchange();
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.METAPROJECT) &&
				exchange.getRequestMethod().equals(Methods.GET)) {
			Serializer<Gson> serl = new DefaultJsonSerializer();
			
			exchange.getResponseSender().send(serl.write(configuration, ServerConfiguration.class));
			
			exchange.endExchange();
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.METAPROJECT) &&
				exchange.getRequestMethod().equals(Methods.POST)) {
			// TODO: After posting the new metaproject, we need to decide what to do with
			//       the config that's loaded in RAM
			Serializer<Gson> serl = new DefaultJsonSerializer();
			
			ServerConfiguration cfg = serl.parse(new InputStreamReader(exchange.getInputStream()), ServerConfiguration.class);
			
			Manager.getConfigurationManager().setServerConfiguration(cfg);
			Manager.getConfigurationManager().saveServerConfiguration(cfgFile);
			exchange.getResponseSender().close();
			
			exchange.endExchange();
			HTTPServer.server().restart();
			
		}
		
	}

}
