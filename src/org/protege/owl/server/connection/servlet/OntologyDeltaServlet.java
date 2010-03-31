package org.protege.owl.server.connection.servlet;

import static org.protege.owl.server.util.OntologyConstants.NS;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.util.ChangeToAxiomConverter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class OntologyDeltaServlet extends HttpServlet {
    private static final long serialVersionUID = 4100741469050997929L;
    public static String PATH = "/ontology/changes";
    
    private Server server;
    
    public OntologyDeltaServlet(Server server) {
        this.server = server;
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
                    OWLOntologyManager manager = summary.getOWLOntologyManager();
                    manager.setOntologyFormat(summary, new OWLXMLOntologyFormat());  // don't use rdf/xml format - owlapi gforge 2959943
                    manager.saveOntology(summary, response.getOutputStream());
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
