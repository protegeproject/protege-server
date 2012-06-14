 package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.IRI;


public class ServerDocument {
	private IRI serverLocation;
	
	public ServerDocument(IRI serverLocation) {
		this.serverLocation = serverLocation;
	}

	public IRI getServerLocation() {
		return serverLocation;
	}
	
}
