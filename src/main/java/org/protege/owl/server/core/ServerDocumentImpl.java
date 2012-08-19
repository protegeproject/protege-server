 package org.protege.owl.server.core;

import java.io.Serializable;

import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.ServerPath;
import org.semanticweb.owlapi.model.IRI;


public abstract class ServerDocumentImpl implements ServerDocument, Serializable {
	private static final long serialVersionUID = -3003767122936738208L;
	private ServerPath serverPath;
	
	public ServerDocumentImpl(ServerPath serverPath) {
		this.serverPath = serverPath;
	}

	@Override
	public ServerPath getServerPath() {
	    return serverPath;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ServerDocument)) {
			return false;
		}
		ServerDocument other = (ServerDocument) o;
		return serverPath.equals(other.getServerPath());
	}
	
	@Override
	public int hashCode() {
	    return serverPath.hashCode();
	}
	
	@Override
	public int compareTo(ServerDocument o) {
	    return serverPath.compareTo(o.getServerPath());
	}
}
