package org.protege.owl.server.api;

import java.io.IOException;

import org.protege.owl.server.impl.RemoteOntologyDocumentImpl;
import org.semanticweb.owlapi.model.IRI;
/*
 * This class represents an ontology saved in a physical location that has versioning information through 
 * an OWL Server associated with it.
 * <p/>
 * It meets the definition of an OWL 2 ontology document because 
 */
public interface VersionedOntologyDocument {

	IRI getLocalAddress();
	
	ChangeDocument getLocalHistory() throws IOException;
	
	void addToLocalHistory(ChangeDocument changes) throws IOException;
	
	RemoteOntologyDocument getServerDocument() throws IOException;
}
