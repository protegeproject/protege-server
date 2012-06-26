 package org.protege.owl.server.impl;

import org.protege.owl.server.api.ServerDocument;
import org.semanticweb.owlapi.model.IRI;


public class ServerDocumentImpl implements ServerDocument {
	private IRI serverLocation;
	
	public ServerDocumentImpl(IRI serverLocation) {
		this.serverLocation = serverLocation;
	}

	@Override
	public IRI getServerLocation() {
		return serverLocation;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ServerDocument)) {
			return false;
		}
		ServerDocument other = (ServerDocument) o;
		return serverLocation.equals(other.getServerLocation());
	}
}
