package org.protege.editor.owl.server.http.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.http.ServerEndpoints;
import org.protege.editor.owl.server.http.ServerProperties;
import org.protege.editor.owl.server.http.exception.ServerException;
import org.protege.editor.owl.server.http.messages.EVSHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.protege.metaproject.api.ServerConfiguration;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;

public class CodeGenHandler extends BaseRoutingHandler {

	private static Logger logger = LoggerFactory.getLogger(CodeGenHandler.class);

	private ProtegeServer server;
	private ServerConfiguration config;
	
	public CodeGenHandler(ProtegeServer s) {
		server = s;
		config = server.getConfiguration();
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) {
		if (exchange.getRequestPath().equalsIgnoreCase(ServerEndpoints.GEN_CODE)) {

			int cnt = 1;
			try {
				String scnt = getQueryParameter(exchange, "count");
				cnt = (new Integer(scnt)).intValue();
			} catch (ServerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			String p = config.getProperty(ServerProperties.CODEGEN_PREFIX);
			String s = config.getProperty(ServerProperties.CODEGEN_SUFFIX);
			String d = config.getProperty(ServerProperties.CODEGEN_DELIMETER);
			String cfn = config.getProperty(ServerProperties.EVS_HISTORY_FILE);
			int seq = 0;
			try {
				File file = new File(cfn);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				seq = Integer.parseInt(bufferedReader.readLine().trim());
				
				List<String> codes = new ArrayList<String>();
				String sseq = "0";
				for (int j = 0; j < cnt; j++) {
					sseq = (new Integer(seq++)).toString();
					String code = p + d + sseq;
					if (s != null) {
						code = code + d + s;
					}
					codes.add(code);		    	
				}			

				ObjectOutputStream os = new ObjectOutputStream(exchange.getOutputStream());
				os.writeObject(codes);

				try {
					fileReader.close();
				}
				catch (IOException e) {
					// Ignore the exception but report it into the log
					logger.warn("Unable to close the file reader stream used to read the code generator configuration");
				}
				flushCode(exchange, file, seq);
			}
			catch (IOException e) {
				internalServerErrorStatusCode(exchange, "Server failed to read code generator configuration", e);
			}
			catch (ServerException e) {
				handleServerException(exchange, e);
			}
			finally {
				exchange.endExchange(); // end the request
			}
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(ServerEndpoints.GEN_CODES)) { 
			// NO-OP
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(ServerEndpoints.EVS_REC)) {
			try {
				ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
				EVSHistory hist = (EVSHistory) ois.readObject();
				recordEvsHistory(exchange, hist);
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
	}

	private void flushCode(HttpServerExchange exchange, File file, int seq) throws ServerException {
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.println(seq + 1);
			pw.close();
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to generate code", e);
		}
	}

	private void recordEvsHistory(HttpServerExchange exchange, EVSHistory hist) throws ServerException {
		try {
			String hisfile = config.getProperty(ServerProperties.EVS_HISTORY_FILE);
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(hisfile, true)));
			pw.println(hist.toRecord());
			pw.close();
		}
		catch (IOException e) {
			throw new ServerException(StatusCodes.INTERNAL_SERVER_ERROR, "Server failed to record EVS history", e);
		}
	}

	private void internalServerErrorStatusCode(HttpServerExchange exchange, String message, Exception cause) {
		logger.error(cause.getMessage(), cause);
		exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
		exchange.getResponseHeaders().add(new HttpString("Error-Message"), message);
		exchange.endExchange(); // end the request
	}

	private void handleServerException(HttpServerExchange exchange, ServerException e) {
		logger.error(e.getCause().getMessage(), e.getCause());
		exchange.setStatusCode(e.getErrorCode());
		exchange.getResponseHeaders().add(new HttpString("Error-Message"), e.getMessage());
	}
}
