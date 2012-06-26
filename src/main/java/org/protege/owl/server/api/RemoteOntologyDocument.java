package org.protege.owl.server.api;

public interface RemoteOntologyDocument extends ServerDocument {
	
	OntologyDocumentRevision getRevision();
	
	void setRevision(OntologyDocumentRevision revision);

}
