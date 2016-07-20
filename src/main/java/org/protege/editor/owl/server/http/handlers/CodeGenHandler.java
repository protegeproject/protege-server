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
import org.protege.editor.owl.server.http.messages.EVSHistory;

import edu.stanford.protege.metaproject.api.ProjectId;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import io.undertow.server.HttpServerExchange;

public class CodeGenHandler extends BaseRoutingHandler {
	
	private ProtegeServer server;
	private ServerConfiguration config;
	
	public CodeGenHandler(ProtegeServer s) {
		server = s;
		config = server.getConfiguration();
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.GEN_CODE)) {
			String p = config.getProperty("codegen_prefix");
			String s = config.getProperty("codegen_suffix");
			String d = config.getProperty("codegen_delimeter");
			String cfn = config.getProperty("codegen_file");


			try {
				File file = new File(cfn);
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				int seq = Integer.parseInt(bufferedReader.readLine().trim());
				String sseq = (new Integer(seq)).toString();
				String resp = p + d + sseq;
				if (s != null) {
					resp = resp + d + s;
				}
				exchange.getResponseSender().send(resp);
				fileReader.close();
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				pw.println(seq + 1);
				pw.close();

			} catch (IOException e) {

			}	
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.GEN_CODES)) { 
			
		} else if (exchange.getRequestPath().equalsIgnoreCase(HTTPServer.EVS_REC)) {
			ObjectInputStream ois = new ObjectInputStream(exchange.getInputStream());
			EVSHistory hist = (EVSHistory) ois.readObject();
			
			String hisfile = config.getProperty("evshistory_file");
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(hisfile, true)));
			pw.println(hist.toRecord());
			pw.close();
			exchange.getResponseSender().close();
			
		}
		exchange.endExchange();
		//HTTPServer.server().restart();
	}
	

}
