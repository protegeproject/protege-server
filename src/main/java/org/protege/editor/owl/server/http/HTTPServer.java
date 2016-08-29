package org.protege.editor.owl.server.http;

import edu.stanford.protege.metaproject.ConfigurationManager;
import edu.stanford.protege.metaproject.api.AuthToken;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.exception.ObjectConversionException;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import io.undertow.util.StatusCodes;
import org.protege.editor.owl.server.api.LoginService;
import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.http.exception.ServerConfigurationInitializationException;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.http.handlers.*;
import org.protege.editor.owl.server.security.SSLContextFactory;
import org.protege.editor.owl.server.security.SSLContextInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

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

	private TokenTable token_table;

	private AuthenticationHandler change_handler, codegen_handler, meta_handler, server_handler;
	private BlockingHandler login_handler;

	private Undertow web_server;

	private Undertow admin_server;
	private boolean isRunning = false;

	private URI uri;
	private int admin_port;

	private io.undertow.server.RoutingHandler web_router;
	private io.undertow.server.RoutingHandler admin_router;

	private GracefulShutdownHandler webRouterHandler;
	private GracefulShutdownHandler adminRouterHandler;

	private static HTTPServer server;

	public static HTTPServer server() {
		return server;
	}

	public void addSession(String key, AuthToken tok) {
		token_table.put(key, tok);
	}

	public AuthToken getAuthToken(String tok) throws ServerException {
		return token_table.get(tok);
	}

	public HTTPServer() {server = this;}
	
	public HTTPServer(String cfn) {
		this.config_fname = cfn;
		server = this;
	}

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
			config = ConfigurationManager.getConfigurationLoader().loadConfiguration(new File(config_fname));
			pserver = new ProtegeServer(config);
			uri = config.getHost().getUri();
			admin_port = config.getHost().getSecondaryPort().get().get();
		}
		catch (FileNotFoundException | ObjectConversionException e) {
			logger.error("Unable to load server configuration at location: " + config_fname, e);
			throw new ServerConfigurationInitializationException("Unable to load server configuration", e);
		}
	}
	
	private BlockingHandler loadAndCreateLogin(ServerConfiguration config) {
		
		String authClassName = config.getProperty(ServerProperties.AUTHENTICATION_CLASS);
		LoginService service = null;
		if (authClassName != null) {
			try {
				service = (LoginService) Class.forName(authClassName).newInstance();
				service.setConfig(config);
				
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new BlockingHandler(new HTTPLoginService(service));
	}

	private TokenTable createLoginTokenTable(ServerConfiguration config) {
		long loginTimeout = TokenTable.DEFAULT_TIMEOUT_PERIOD;
		String loginTimeoutValue = config.getProperty(ServerProperties.LOGIN_TIMEOUT_PERIOD);
		if (loginTimeoutValue != null && !loginTimeoutValue.isEmpty()) {
			loginTimeout = Long.parseLong(loginTimeoutValue);
		}
		return new TokenTable(loginTimeout);
	}

	public void start() throws ServerConfigurationInitializationException, SSLContextInitializationException {
		initConfig();
		
		web_router = Handlers.routing();
		admin_router = Handlers.routing();
		
		// create login handler
		login_handler = loadAndCreateLogin(config);
		token_table = createLoginTokenTable(config);
		
		web_router.add("POST", LOGIN, login_handler);
		admin_router.add("POST", LOGIN, login_handler);
		
		// create change service handler
		change_handler = new AuthenticationHandler(new BlockingHandler(new HTTPChangeService(pserver)));
		web_router.add("POST", COMMIT,  change_handler);
		web_router.add("POST", HEAD,  change_handler);
		web_router.add("POST", LATEST_CHANGES,  change_handler);
		web_router.add("POST", ALL_CHANGES,  change_handler);
		
		// create code generator handler
		codegen_handler = new AuthenticationHandler(new BlockingHandler(new CodeGenHandler(pserver)));
		web_router.add("GET", GEN_CODE, codegen_handler);
		web_router.add("GET", GEN_CODES, codegen_handler);
		web_router.add("POST", EVS_REC, codegen_handler);
		
		// create mataproject handler
		meta_handler = new AuthenticationHandler(new BlockingHandler(new MetaprojectHandler(pserver)));
		web_router.add("GET", METAPROJECT, meta_handler);
		admin_router.add("GET", METAPROJECT, meta_handler);
		admin_router.add("POST", METAPROJECT, meta_handler);
		web_router.add("GET", PROJECT,  meta_handler);
		admin_router.add("POST", PROJECT,  meta_handler);
		admin_router.add("POST", PROJECT_SNAPSHOT,  meta_handler);
		web_router.add("POST", PROJECT_SNAPSHOT_GET,  meta_handler);
		admin_router.add("DELETE", PROJECT,  meta_handler);
		web_router.add("GET", PROJECTS, meta_handler);
		
		// create server handler
		server_handler = new AuthenticationHandler(new BlockingHandler(new HTTPServerHandler()));
		admin_router.add("POST", SERVER_RESTART, server_handler);
		admin_router.add("POST", SERVER_STOP, server_handler);
		
		// Build the servers
		webRouterHandler = Handlers.gracefulShutdown(Handlers.exceptionHandler(web_router));
		adminRouterHandler = Handlers.gracefulShutdown(Handlers.exceptionHandler(admin_router));
		
		logger.info("Starting server instances");
		if (uri.getScheme().equalsIgnoreCase("https")) {
			SSLContext ctx = new SSLContextFactory().createSslContext();
			web_server = Undertow.builder()
					.addHttpsListener(uri.getPort(), uri.getHost(), ctx)
					.setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
					.setHandler(webRouterHandler)
					.build();
			web_server.start();
			logger.info("... Project server has started at port " + uri.getPort());
			
			admin_server = Undertow.builder()
					.addHttpsListener(admin_port, uri.getHost(), ctx)
					.setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
					.setHandler(adminRouterHandler)
					.build();
			admin_server.start();
			logger.info("... Admin server has started at port " + admin_port);
		}
		else {
			web_server = Undertow.builder()
					.addHttpListener(uri.getPort(), uri.getHost())
					.setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
					.setHandler(webRouterHandler)
					.build();
			web_server.start();
			logger.info("... Project server has started at port " + uri.getPort());
			
			admin_server = Undertow.builder()
					.addHttpListener(admin_port, uri.getHost())
					.setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
					.setHandler(adminRouterHandler)
					.build();
			admin_server.start();
			logger.info("... Admin server has started at port " + admin_port);
		}
		isRunning = true;
	}

	public void stop() throws ServerException {
		if (isRunning) {
			logger.info("Stopping server instances");
			try {
				if (web_server != null) {
					if (webRouterHandler != null) {
						webRouterHandler.shutdown();
					}
					web_server.stop();
					web_server = null;
					logger.info("... Project server has stopped");
				}
				if (admin_server != null) {
					if (adminRouterHandler != null) {
						adminRouterHandler.shutdown();
					}
					admin_server.stop();
					admin_server = null;
					logger.info("... Admin server has stopped");
				}
				isRunning = false;
			}
			catch (Exception e) {
				throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
			}
		}
	}

	public void restart() throws ServerException {
		try {
			logger.info("Received request to restart");
			stop();
			start();
		}
		catch (ServerConfigurationInitializationException | SSLContextInitializationException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}
