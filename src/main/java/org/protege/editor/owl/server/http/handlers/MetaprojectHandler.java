package org.protege.editor.owl.server.http.handlers;

import java.io.File;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.change.ChangeManagementFilter;
import org.protege.editor.owl.server.http.HTTPServer;
import org.protege.editor.owl.server.versioning.api.ServerDocument;

import com.google.gson.Gson;

import edu.stanford.protege.metaproject.Manager;
import edu.stanford.protege.metaproject.api.AuthenticationRegistry;
import edu.stanford.protege.metaproject.api.Description;
import edu.stanford.protege.metaproject.api.MetaprojectAgent;
import edu.stanford.protege.metaproject.api.MetaprojectFactory;
import edu.stanford.protege.metaproject.api.Name;
import edu.stanford.protege.metaproject.api.OperationRegistry;
import edu.stanford.protege.metaproject.api.Policy;
import edu.stanford.protege.metaproject.api.Project;
import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ProjectOptions;
import edu.stanford.protege.metaproject.api.ProjectRegistry;
import edu.stanford.protege.metaproject.api.RoleRegistry;
import edu.stanford.protege.metaproject.api.Serializer;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.UserRegistry;
import edu.stanford.protege.metaproject.serialization.DefaultJsonSerializer;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;

public class MetaprojectHandler extends BaseRoutingHandler {
	
	private ServerConfiguration configuration;
	private ProtegeServer server;
	private ChangeManagementFilter cf;

    private AuthenticationRegistry authenticationRegistry;
    private UserRegistry userRegistry;
    private ProjectRegistry projectRegistry;
    private RoleRegistry roleRegistry;
    private OperationRegistry operationRegistry;
    private Policy policy;
    private MetaprojectAgent metaprojectAgent;
    
    private File cfgFile;
    
    public MetaprojectHandler(ProtegeServer pserver) {
    	server = pserver;
    	cf = new ChangeManagementFilter(server);
    	configuration = server.getConfiguration();
        authenticationRegistry = configuration.getAuthenticationRegistry();
        userRegistry = configuration.getMetaproject().getUserRegistry();
        projectRegistry = configuration.getMetaproject().getProjectRegistry();
        roleRegistry = configuration.getMetaproject().getRoleRegistry();
        policy = configuration.getMetaproject().getPolicy();
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

			List<Project> projects = new ArrayList<>(metaprojectAgent.getProjects(userId));
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());        
			os.writeObject(projects);
			exchange.endExchange();
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.CREATE_PROJECT) &&
				exchange.getRequestMethod().equals(Methods.POST)) {
			
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			ProjectId pid = (ProjectId) ois.readObject();
			Name pname = (Name) ois.readObject();
			Description desc = (Description) ois.readObject();
			UserId uid = (UserId) ois.readObject();
			ProjectOptions opts = (ProjectOptions) ois.readObject();
			Optional<ProjectOptions> oopts = Optional.ofNullable(opts);		
			
			ServerDocument doc = cf.createProject(null, pid, pname, desc, uid, oopts);			
			
	        ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
	        
	        os.writeObject(doc);
			exchange.endExchange();
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.PROJECT)) {
			String pid = super.getQueryParameter(exchange, "projectid");

			MetaprojectFactory f = Manager.getFactory();

			ProjectId projId  = f.getProjectId(pid);
			ServerDocument sdoc = server.openProject(null, projId);
			ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
	        
	        os.writeObject(sdoc);
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
			
			exchange.endExchange();
			
		}
		
	}

}
