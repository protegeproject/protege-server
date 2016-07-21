package org.protege.editor.owl.server.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.protege.editor.owl.server.api.exception.AuthorizationException;
import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.http.handlers.AuthenticationHandler;
import org.protege.editor.owl.server.http.handlers.CodeGenHandler;
import org.protege.editor.owl.server.http.handlers.HTTPChangeService;
import org.protege.editor.owl.server.http.handlers.HTTPLoginService;
import org.protege.editor.owl.server.http.handlers.MetaprojectHandler;
import org.protege.editor.owl.server.security.DefaultLoginService;
import org.protege.editor.owl.server.security.SSLContextFactory;
import org.protege.editor.owl.server.security.SessionManager;

import edu.stanford.protege.metaproject.Manager;
import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.AuthenticationRegistry;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.UserRegistry;
import edu.stanford.protege.metaproject.api.exception.ObjectConversionException;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.util.StatusCodes;

public final class HTTPServer {

	private static final Logger LOGGER = Logger.getLogger(HTTPServer.class.getName());

	public static final String SERVER_CONFIGURATION_PROPERTY = "org.protege.owl.server.configuration";

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 8080;

	public static final String ROOT_PATH = "/nci_protege";
	
	public static final String LOGIN = ROOT_PATH + "/login";
	
	public static final String PROJECT = ROOT_PATH + "/meta/project";
	public static final String PROJECT_SNAPSHOT = ROOT_PATH + "/meta/project/snapshot";
	public static final String PROJECT_SNAPSHOT_GET = ROOT_PATH + "/meta/project/snapshot/get";
	public static final String PROJECTS = ROOT_PATH + "/meta/projects";
	public static final String METAPROJECT = ROOT_PATH + "/meta/metaproject";
	
	public static final String ALL_CHANGES = ROOT_PATH + "/all_changes"; 
	public static final String LATEST_CHANGES = ROOT_PATH + "/latest_changes"; 
	public static final String HEAD = ROOT_PATH + "/head";
	public static final String COMMIT = ROOT_PATH + "/commit";
	
	public static final String GEN_CODE = ROOT_PATH + "/gen_code";
	public static final String GEN_CODES = ROOT_PATH + "/gen_codes";
	public static final String EVS_REC = ROOT_PATH + "/evs_record";
	
	private String config_fname = null;
	private ServerConfiguration config;
	private ProtegeServer pserver;

	private SessionManager session_manager = new SessionManager();
	private TokenTable token_table = new TokenTable();
	
	private AuthenticationHandler change_handler, codegen_handler, meta_handler;
	private BlockingHandler login_handler;

	private Undertow web_server;
	private boolean isRunning = false;


	private URI uri;
	private io.undertow.server.RoutingHandler router;

	private static HTTPServer server;

	public static HTTPServer server() {
		return server;
	}

	public void addSession(String key, AuthToken tok) {
		session_manager.add(tok);
		token_table.put(key, tok);
	}

	public AuthToken getAuthToken(String tok) throws AuthorizationException {
		return token_table.get(tok);		
	}

	public HTTPServer() {server = this;}
	
	public HTTPServer(String cfn) {
		this.config_fname = cfn;
		server = this;}


	public static void main(final String[] args) {

		HTTPServer s = new HTTPServer();	
		try {
			s.start();
		} catch (ServerException e) {
			e.printStackTrace();
		}

	}
	
	private void initConfig() throws ServerException {
		try {
			if (config_fname == null) {
				config_fname = System.getProperty(SERVER_CONFIGURATION_PROPERTY);
			}

			config = Manager.getConfigurationManager().loadServerConfiguration(new File(config_fname));

			pserver = new ProtegeServer(config); 			

			uri = config.getHost().getUri();
			

		}
		catch (FileNotFoundException | ObjectConversionException  e) {
			e.printStackTrace();
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR);
		}
		
	}

	public void start() throws ServerException {


		initConfig();

		
		router = Handlers.routing();

		// create login handler
		AuthenticationRegistry authRegistry = config.getAuthenticationRegistry();
		UserRegistry userRegistry = config.getMetaproject().getUserRegistry();
		DefaultLoginService loginService = new DefaultLoginService(authRegistry, userRegistry, session_manager);

		login_handler = new BlockingHandler(new HTTPLoginService(loginService));

		router.add("POST", LOGIN, login_handler);

		// create change service handler
		change_handler = new AuthenticationHandler(
				new BlockingHandler(
						new HTTPChangeService(
								pserver)));

		router.add("POST", COMMIT,  change_handler);
		router.add("POST", HEAD,  change_handler);
		router.add("POST", LATEST_CHANGES,  change_handler);
		router.add("POST", ALL_CHANGES,  change_handler);
		
		

		// create code generator handler
		codegen_handler = new AuthenticationHandler(
				new BlockingHandler(
						new CodeGenHandler(pserver)));

		router.add("GET", GEN_CODE, codegen_handler);
		router.add("GET", GEN_CODES, codegen_handler);
		router.add("POST", EVS_REC, codegen_handler);

		// create mataproject handler        
		meta_handler = new AuthenticationHandler(
				new BlockingHandler(
						new MetaprojectHandler(pserver)));
		router.add("GET", METAPROJECT, meta_handler);
		router.add("POST", METAPROJECT, meta_handler);
		router.add("GET", PROJECT,  meta_handler);
		router.add("POST", PROJECT,  meta_handler);
		router.add("POST", PROJECT_SNAPSHOT,  meta_handler);
		router.add("POST", PROJECT_SNAPSHOT_GET,  meta_handler);
		router.add("DELETE", PROJECT,  meta_handler);
		router.add("GET", PROJECTS, meta_handler);
		
		
		
		final ExceptionHandler aExceptionHandler = Handlers.exceptionHandler(router);

		final GracefulShutdownHandler aShutdownHandler = Handlers.gracefulShutdown(aExceptionHandler);
		
		router.add("GET", ROOT_PATH + "/admin/restart", new HttpHandler() {
			@Override
			public void handleRequest(final HttpServerExchange exchange) throws Exception {
				aShutdownHandler.shutdown();
				aShutdownHandler.addShutdownListener(new GracefulShutdownHandler.ShutdownListener() {
					@Override
					public void shutdown(final boolean isDown) {
						if (isDown) {
							restart();
						}
					}
				});
				exchange.endExchange();
			}
		});

		try {

			if (uri.getScheme().equalsIgnoreCase("https")) {
				SSLContext ctx = new SSLContextFactory().createSslContext();
				web_server = Undertow.builder()
						.addHttpsListener(uri.getPort(), uri.getHost(), ctx)
						.setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
						.setHandler(aShutdownHandler)
						.build();

			} else {
				web_server = Undertow.builder()
						.addHttpListener(uri.getPort(), uri.getHost())
						.setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
						.setHandler(aShutdownHandler)
						.build();
			}


		} catch (Exception e) {
			e.printStackTrace();
		}		


		isRunning = true;
		web_server.start();
		System.out.println("System has started...");

	}


	public void stop() {
		if (web_server != null && isRunning) {
			System.out.println("Received request to shutdown");
			System.out.println("System is shutting down...");

			web_server.stop();
			web_server = null;
			isRunning = false;
		}
	}
	
	public void restart() {
		stop();
		try {
			start();
		} catch (ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
