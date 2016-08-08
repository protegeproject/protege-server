package org.protege.editor.owl.server.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import javax.net.ssl.SSLContext;

import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.http.exception.ServerConfigurationInitializationException;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.http.handlers.AuthenticationHandler;
import org.protege.editor.owl.server.http.handlers.CodeGenHandler;
import org.protege.editor.owl.server.http.handlers.HTTPChangeService;
import org.protege.editor.owl.server.http.handlers.HTTPLoginService;
import org.protege.editor.owl.server.http.handlers.HTTPServerHandler;
import org.protege.editor.owl.server.http.handlers.MetaprojectHandler;
import org.protege.editor.owl.server.security.DefaultLoginService;
import org.protege.editor.owl.server.security.SSLContextFactory;
import org.protege.editor.owl.server.security.SSLContextInitializationException;
import org.protege.editor.owl.server.security.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

public final class HTTPServer {

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
	
	public static final String SERVER_RESTART = ROOT_PATH + "/server/restart";
	public static final String SERVER_STOP = ROOT_PATH + "/server/stop";
	
	private static Logger logger = LoggerFactory.getLogger(HTTPServer.class);
	
	private String config_fname = null;
	private ServerConfiguration config;
	private ProtegeServer pserver;

	private SessionManager session_manager = new SessionManager();
	private TokenTable token_table = new TokenTable();
	
	private AuthenticationHandler change_handler, codegen_handler, meta_handler, server_handler;
	private BlockingHandler login_handler;

	private Undertow web_server;
	
	private Undertow admin_server;
	private boolean isRunning = false;


	private URI uri;
	private int admin_port;
	
	private io.undertow.server.RoutingHandler web_router;
	private io.undertow.server.RoutingHandler admin_router;

	private static HTTPServer server;

	public static HTTPServer server() {
		return server;
	}

	public void addSession(String key, AuthToken tok) {
		session_manager.add(tok);
		token_table.put(key, tok);
	}

	public AuthToken getAuthToken(String tok) throws ServerException {
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
		}
		catch (ServerConfigurationInitializationException | SSLContextInitializationException e) {
			// NO-OP: The exceptions are already logged by the module that generates the exception
		}
	}
	
	private void initConfig() throws ServerConfigurationInitializationException {
		try {
			if (config_fname == null) {
				config_fname = System.getProperty(SERVER_CONFIGURATION_PROPERTY);
			}
			config = Manager.getConfigurationManager().loadServerConfiguration(new File(config_fname));
			pserver = new ProtegeServer(config);
			uri = config.getHost().getUri();
			admin_port = config.getHost().getSecondaryPort().get().get();
		}
		catch (FileNotFoundException | ObjectConversionException e) {
			logger.error("Unable to load server configuration at location: " + config_fname, e);
			throw new ServerConfigurationInitializationException("Unable to load server configuration", e);
		}
	}

	public void start() throws ServerConfigurationInitializationException, SSLContextInitializationException {

		initConfig();
		
		web_router = Handlers.routing();
		admin_router = Handlers.routing();

		// create login handler
		AuthenticationRegistry authRegistry = config.getAuthenticationRegistry();
		UserRegistry userRegistry = config.getMetaproject().getUserRegistry();
		
		DefaultLoginService loginService = new DefaultLoginService(authRegistry, userRegistry, session_manager);

		login_handler = new BlockingHandler(new HTTPLoginService(loginService));

		
		web_router.add("POST", LOGIN, login_handler);
		admin_router.add("POST", LOGIN, login_handler);

		// create change service handler
		change_handler = new AuthenticationHandler(
				new BlockingHandler(
						new HTTPChangeService(
								pserver)));

		web_router.add("POST", COMMIT,  change_handler);
		web_router.add("POST", HEAD,  change_handler);
		web_router.add("POST", LATEST_CHANGES,  change_handler);
		web_router.add("POST", ALL_CHANGES,  change_handler);
		
		

		// create code generator handler
		codegen_handler = new AuthenticationHandler(
				new BlockingHandler(
						new CodeGenHandler(pserver)));

		web_router.add("GET", GEN_CODE, codegen_handler);
		web_router.add("GET", GEN_CODES, codegen_handler);
		web_router.add("POST", EVS_REC, codegen_handler);

		// create mataproject handler        
		meta_handler = new AuthenticationHandler(
				new BlockingHandler(
						new MetaprojectHandler(pserver)));
		web_router.add("GET", METAPROJECT, meta_handler);
		
		admin_router.add("GET", METAPROJECT, meta_handler);		
		admin_router.add("POST", METAPROJECT, meta_handler);
		
		
		web_router.add("GET", PROJECT,  meta_handler);
		web_router.add("POST", PROJECT,  meta_handler);
		web_router.add("POST", PROJECT_SNAPSHOT,  meta_handler);
		web_router.add("POST", PROJECT_SNAPSHOT_GET,  meta_handler);
		web_router.add("DELETE", PROJECT,  meta_handler);
		web_router.add("GET", PROJECTS, meta_handler);
		
		// create server handler
		server_handler = new AuthenticationHandler(new BlockingHandler(new HTTPServerHandler()));
		admin_router.add("POST", SERVER_RESTART, server_handler);
		admin_router.add("POST", SERVER_STOP, server_handler);
		
		final ExceptionHandler aExceptionHandler = Handlers.exceptionHandler(web_router);
		
		final ExceptionHandler aExceptionHandler2 = Handlers.exceptionHandler(admin_router);

		final GracefulShutdownHandler aShutdownHandler = Handlers.gracefulShutdown(aExceptionHandler);
		final GracefulShutdownHandler aShutdownHandler2 = Handlers.gracefulShutdown(aExceptionHandler2);
		
		admin_router.add("GET", ROOT_PATH + "/admin/restart", new HttpHandler() {
			@Override
			public void handleRequest(final HttpServerExchange exchange) throws Exception {
				aShutdownHandler.shutdown();
				aShutdownHandler.addShutdownListener(new GracefulShutdownHandler.ShutdownListener() {
					@Override
					public void shutdown(final boolean isDown) {
						if (isDown) {
							try {
								restart();
							}
							catch (ServerException e) {
								exchange.setStatusCode(e.getErrorCode());
								exchange.getResponseHeaders().add(new HttpString("Error-Message"), e.getMessage());
								exchange.endExchange();
							}
						}
					}
				});
				exchange.endExchange();
			}
		});

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
			
			admin_server = Undertow.builder()
					.addHttpListener(admin_port, uri.getHost())
					.setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
					.setHandler(aShutdownHandler2)
					.build();
		}

		isRunning = true;
		web_server.start();
		admin_server.start();
		logger.info("System has started...");

	}


	public void stop() throws ServerException {
		if (web_server != null && isRunning) {
			try {
				logger.info("Received request to shutdown");
				logger.info("System is shutting down...");
	
				web_server.stop();
				web_server = null;
				admin_server.stop();
				admin_server = null;
				isRunning = false;
			}
			catch (Exception e) {
				throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
	}
	
	public void restart() throws ServerException {
		try {
			stop();
			start();
		}
		catch (ServerConfigurationInitializationException | SSLContextInitializationException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}
