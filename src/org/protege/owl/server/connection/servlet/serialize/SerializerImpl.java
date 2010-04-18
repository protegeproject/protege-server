package org.protege.owl.server.connection.servlet.serialize;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.coode.owlapi.owlxmlparser.OWLXMLParser;
import org.protege.owl.server.exception.OntologyConflictException;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owl.server.exception.RuntimeOntologyConflictException;
import org.protege.owl.server.exception.RuntimeServerException;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class SerializerImpl implements Serializer {
    /* ************************************************
     * don't use rdf/xml format - owlapi gforge 2959943
     */
    @Override
    public void serialize(OWLOntology ontology, OutputStream stream) throws OWLOntologyStorageException {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        manager.setOntologyFormat(ontology, new OWLXMLOntologyFormat());
        manager.saveOntology(ontology, stream);
    }
    
    @Override
    public OWLOntology deserialize(OWLOntologyManager manager, URL url) throws IOException, OntologyConflictException, RemoteQueryException  {
        OWLXMLParser parser = new OWLXMLParser();
        parser.setOWLOntologyManager(manager);
        boolean success = false;
        OWLOntology ontology = null;
        try {
            ontology = manager.createOntology();
            parser.parse(new ServletDocumentSource(url), ontology);
            success = true;
            return ontology;
        }
        catch (RuntimeOntologyConflictException conflict) {
            throw new OntologyConflictException(conflict.getRejectedChanges());
        }
        catch (RuntimeServerException e) {
            throw new RemoteQueryException(e);
        }
        catch (OWLParserException e) {
            throw new RemoteQueryException(e);
        }
        catch (OWLOntologyCreationException e) {
            throw new RemoteQueryException(e);
        }
        finally {
            if (!success && ontology != null) {
                manager.removeOntology(ontology);
            }
        }
        
    }

    @Override
    public void serializeException(Throwable t) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
