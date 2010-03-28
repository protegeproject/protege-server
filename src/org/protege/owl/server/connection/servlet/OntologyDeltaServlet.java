package org.protege.owl.server.connection.servlet;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.api.Server;
import org.semanticweb.owlapi.apibinding.OWLManager;
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
    public static final String NS = "http://protege.stanford.edu/ontologies/OntologyChanges.owl";
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
                    final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                    final OWLOntology ontology = manager.createOntology(IRI.create(NS));
                    OWLOntologyChangeVisitor visitor = new OWLOntologyChangeVisitor() {

                        @Override
                        public void visit(AddAxiom change) {
                            manager.addAxiom(ontology, change.getAxiom());
                        }

                        @Override
                        public void visit(RemoveAxiom change) {
                            manager.removeAxiom(ontology, change.getAxiom());
                        }

                        @Override
                        public void visit(SetOntologyID change) {
                            ;
                        }

                        @Override
                        public void visit(AddImport change) {
                            manager.applyChange(new AddImport(ontology, change.getImportDeclaration()));
                        }

                        @Override
                        public void visit(RemoveImport change) {
                            manager.applyChange(new RemoveImport(ontology, change.getImportDeclaration()));
                        }

                        @Override
                        public void visit(AddOntologyAnnotation change) {
                            manager.applyChange(new AddOntologyAnnotation(ontology, change.getAnnotation()));
                        }

                        @Override
                        public void visit(RemoveOntologyAnnotation change) {
                            manager.applyChange(new RemoveOntologyAnnotation(ontology, change.getAnnotation()));
                        }

                    };
                    for (OWLOntologyChange change : server.getChanges(revisions.getOntologyName(), revision1, revision2)) {
                        change.accept(visitor);
                    }
                    manager.setOntologyFormat(ontology, new RDFXMLOntologyFormat());
                    manager.saveOntology(ontology, response.getOutputStream());
                    return;
                }
            }
        }
        catch (IOException io) {
            throw io;
        }
        catch (Throwable t) {
            throw new IOException(t);
        }
    }

}
