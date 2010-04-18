package org.protege.owl.server.connection.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OntologyCommitServlet extends HttpServlet {
    private static final long serialVersionUID = -2089045681752989817L;
    public static final String PATH="/ontology/commit";
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
    }

}
