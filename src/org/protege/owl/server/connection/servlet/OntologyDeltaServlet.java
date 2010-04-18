package org.protege.owl.server.connection.servlet;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.connection.servlet.serialize.Serializer;
import org.protege.owl.server.connection.servlet.serialize.SerializerFactory;
import org.protege.owl.server.util.ChangeToAxiomConverter;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OntologyDeltaServlet extends HttpServlet {
    private static final long serialVersionUID = 4100741469050997929L;
    public static String PATH = "/ontology/changes";
    
    private Server server;
    private Serializer serializer;
    
    public OntologyDeltaServlet(Server server) {
        this.server = server;
        serializer = new SerializerFactory().createSerializer();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String encoded = request.getPathInfo();
            StringTokenizer tokenizer = new StringTokenizer(encoded, "/");
            String shortName = tokenizer.nextToken();
            int revision1 = Integer.parseInt(tokenizer.nextToken());
            int revision2 = Integer.parseInt(tokenizer.nextToken());
            for (RemoteOntologyRevisions revisions: server.getOntologyList()) {
                if (revisions.getShortName().equals(shortName)) {
                	ChangeToAxiomConverter visitor = new ChangeToAxiomConverter();
                    for (OWLOntologyChange change : server.getChanges(revisions.getOntologyName(), revision1, revision2)) {
                    	change.accept(visitor);
                    }
                    OWLOntology summary = visitor.getOntology();
                    serializer.serialize(summary, response.getOutputStream());
                    return;
                }
            }
        }
        catch (IOException io) {
            throw io;
        }
        catch (Throwable t) {
            IOException ioe = new IOException(t.getMessage());
            ioe.initCause(t);
            throw ioe;
        }
    }

}
