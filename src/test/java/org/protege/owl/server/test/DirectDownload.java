package org.protege.owl.server.test;

import org.protege.owl.server.api.ClientConnection;
import org.protege.owl.server.connection.servlet.ServletClientConnection;
import org.protege.owl.server.exception.RemoteQueryException;
import org.protege.owlapi.apibinding.ProtegeOWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class DirectDownload {
	public static void main(String args[]) throws OWLOntologyCreationException, RemoteQueryException {
		ClientConnection connection = new ServletClientConnection(ProtegeOWLManager.createOWLOntologyManager(), "localhost:8080");
		OWLOntology ontology = connection.pull(IRI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl"), 1181994);
	}
}
