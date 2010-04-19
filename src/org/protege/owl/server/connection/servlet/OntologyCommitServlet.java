package org.protege.owl.server.connection.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.protege.owl.server.api.ServerOntologyInfo;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.connection.servlet.serialize.Serializer;
import org.protege.owl.server.connection.servlet.serialize.SerializerFactory;
import org.protege.owl.server.exception.OntologyConflictException;
import org.protege.owl.server.exception.RemoteOntologyException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owl.server.util.AxiomToChangeConverter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OntologyCommitServlet extends HttpServlet {
    private static final long serialVersionUID = -2089045681752989817L;
    public static final String PATH="/ontology/commit";
    
    private Server server;
    private Serializer serializer;
    
    public OntologyCommitServlet(Server server) {
        this.server = server;
        this.serializer = new SerializerFactory().createSerializer();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        OWLOntologyManager otherManager = OWLManager.createOWLOntologyManager();
        OWLOntology changeOntology;
        try {
            changeOntology = serializer.deserialize(otherManager, new StreamDocumentSource(request.getInputStream()));
        }
        catch (RemoteQueryException re) {
            response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
            throw new ServletException(re);
        }
        AxiomToChangeConverter converter = new AxiomToChangeConverter(changeOntology, server.getOntologies());
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLAxiom axiom : changeOntology.getAxioms()) {
            axiom.accept(converter);
            OWLOntologyChange change = converter.getChange();
            if (change != null) {
                changes.add(change);
            }
        }
        try {
            server.applyChanges(converter.getCurrentRevisionMap(), changes);
        } catch (OntologyConflictException e) {
            response.setStatus(HttpURLConnection.HTTP_CONFLICT);
            throw new ServletException(e);
        }
        catch (RemoteOntologyException e) {
            throw new ServletException(e);
        }
    }

}
