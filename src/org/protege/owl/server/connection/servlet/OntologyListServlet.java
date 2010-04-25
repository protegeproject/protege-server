package org.protege.owl.server.connection.servlet;

import static org.protege.owl.server.util.OntologyConstants.NS;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MARKED_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_MAX_REVISION_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.ONTOLOGY_SHORT_NAME_PROPERTY;
import static org.protege.owl.server.util.OntologyConstants.REMOTE_ONTOLOGY_CLASS;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.protege.owl.server.api.ServerOntologyInfo;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.connection.servlet.serialize.Serializer;
import org.protege.owl.server.connection.servlet.serialize.SerializerFactory;
import org.protege.owl.server.util.OntologyConstants;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


public class OntologyListServlet extends HttpServlet {
    private static final long serialVersionUID = -3456442049571420576L;
    
    public static final String PATH="/ontology/list";
    
    private Server server;
    private Serializer serializer;
        
    public OntologyListServlet(Server server) {
        this.server = server;
        serializer = new SerializerFactory().createSerializer();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager(OntologyConstants.factory);
            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLOntology ontology = manager.createOntology(IRI.create(NS));
            manager.setOntologyFormat(ontology, new RDFXMLOntologyFormat());
            int counter = 0;
            for (ServerOntologyInfo revisions : server.getOntologyInfoByIRI().values()) {
                OWLNamedIndividual o = factory.getOWLNamedIndividual(IRI.create(NS + "#ontology" + (counter++)));
                manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(REMOTE_ONTOLOGY_CLASS, o));
                OWLLiteral name = factory.getOWLStringLiteral(revisions.getOntologyName().toString());
                manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_NAME_PROPERTY, o, name));
                OWLLiteral revision = factory.getOWLTypedLiteral(revisions.getMaxRevision());
                manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_MAX_REVISION_PROPERTY, o, revision));
                OWLLiteral shortName = factory.getOWLStringLiteral(revisions.getShortName());
                manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_SHORT_NAME_PROPERTY, o, shortName));
                for (Integer markedRevision : revisions.getMarkedRevisions()) {
                    OWLLiteral markedRevisionAsLiteral = factory.getOWLTypedLiteral(markedRevision);
                    manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(ONTOLOGY_MARKED_REVISION_PROPERTY, o, markedRevisionAsLiteral));
                }
            }
            response.setContentType("text/xml;charset=UTF-8");
            serializer.serialize(ontology, response.getOutputStream());
        }
        catch (OWLOntologyCreationException e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
        catch (OWLOntologyStorageException e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }
}
