package org.protege.owl.server.connection.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.protege.owl.server.api.ServerOntologyInfo;
import org.protege.owl.server.api.Server;

public class MarkedOntologyServlet extends HttpServlet {
    private static final long serialVersionUID = -6826342078353081611L;
    
    public static final String PATH="/ontology/download";
    
    private Server server;

    public MarkedOntologyServlet(Server server) {
        this.server = server;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String encoded = request.getPathInfo();
        StringTokenizer tokenizer = new StringTokenizer(encoded, "/");
        String shortName = tokenizer.nextToken();
        Integer revision = Integer.parseInt(tokenizer.nextToken());
        ServerOntologyInfo ontologyInfo = server.getOntologyInfoByShortName().get(shortName);
        InputStream in = server.getOntologyStream(ontologyInfo.getOntologyName(), revision);
        in = new BufferedInputStream(in);
        OutputStream out = new BufferedOutputStream(response.getOutputStream());
        int c;
        while ((c = in.read()) != -1) {
            out.write(c);
        }
        out.flush();
        out.close();
        return;
    }
}
