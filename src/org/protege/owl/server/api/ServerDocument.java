package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.IRI;


public interface ServerDocument {
	
	IRI getLocation();
	
	boolean isOntologyDocument();
	
	OntologyDocument asOntologyDocument();
	
	boolean isDirectory();
	
	ServerDirectory asServerDirectory();

}
