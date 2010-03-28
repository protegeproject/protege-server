package org.protege.owl.server.connection.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.protege.owl.server.api.RemoteOntologyRevisions;
import org.protege.owl.server.api.Server;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class OntologyListServlet extends HttpServlet {
    private static final long serialVersionUID = -3456442049571420576L;
    
    public static final String PATH="/ontologies/list";
    
    public static final OWLDataFactory factory = new OWLDataFactoryImpl();
    
    public static final String NS = "http://protege.stanford.edu/ontologies/ServerList.owl";
    public static final OWLClass REMOTE_ONTOLOGY_CLASS = factory.getOWLClass(IRI.create(NS + "#RemoteOntology"));
    public static final OWLDataProperty ONTOLOGY_NAME_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasOntologyName"));
    public static final OWLDataProperty ONTOLOGY_SHORT_NAME_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#ontologyShortName"));
    public static final OWLDataProperty ONTOLOGY_MAX_REVISION_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasMaxOntologyRevision"));
    public static final OWLDataProperty ONTOLOGY_MARKED_REVISION_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#hasMarkedRevision"));
    
    private Server server;
    
    private OWLClass remoteOntologyClass;
    
    public OntologyListServlet(Server server) {
        this.server = server;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager(factory);
            OWLDataFactory factory = manager.getOWLDataFactory();
            OWLOntology ontology = manager.createOntology(IRI.create(NS));
            manager.setOntologyFormat(ontology, new RDFXMLOntologyFormat());
            int counter = 0;
            for (RemoteOntologyRevisions revisions : server.getOntologyList()) {
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
            manager.saveOntology(ontology, response.getOutputStream());
        }
        catch (OWLOntologyCreationException e) {
            throw new IOException(e);
        }
        catch (OWLOntologyStorageException e) {
            throw new IOException(e);
        }
    }
}
