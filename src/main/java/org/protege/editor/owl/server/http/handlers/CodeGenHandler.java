package org.protege.editor.owl.server.http.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.protege.editor.owl.server.base.ProtegeServer;
import org.protege.editor.owl.server.http.HTTPServer;

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
		exchange.endExchange();
		//HTTPServer.server().restart();
	}
	

}
