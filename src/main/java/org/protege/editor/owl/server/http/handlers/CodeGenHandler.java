package org.protege.editor.owl.server.http.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;

import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.http.HTTPServer;
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
		if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.GEN_CODE)) {
			String p = config.getProperty("codegen_prefix");
			String s = config.getProperty("codegen_suffix");
			String d = config.getProperty("codegen_delimeter");
			String cfn = config.getProperty("codegen_file");
			int seq = 0;
			try {
				File file = new File(cfn);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				seq = Integer.parseInt(bufferedReader.readLine().trim());
				String sseq = (new Integer(seq)).toString();
				String resp = p + d + sseq;
				if (s != null) {
					resp = resp + d + s;
				}
				exchange.getResponseSender().send(resp);
				try {
					fileReader.close();
				}
				catch (IOException e) {
					// Ignore the exception but report it into the log
					logger.warn("Unable to close the file reader stream used to read the code generator configuration");
				}
				generateCode(exchange, file, seq);
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
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.GEN_CODES)) { 
			// NO-OP
		}
		else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.EVS_REC)) {
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

	private void generateCode(HttpServerExchange exchange, File file, int seq) throws ServerException {
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
			String hisfile = config.getProperty("evshistory_file");
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
