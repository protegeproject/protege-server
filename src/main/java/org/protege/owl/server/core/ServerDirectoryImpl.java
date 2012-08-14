package org.protege.owl.server.core;

import org.protege.owl.server.api.ServerDirectory;
import org.semanticweb.owlapi.model.IRI;

public class ServerDirectoryImpl extends ServerDocumentImpl implements ServerDirectory {
	private static final long serialVersionUID = 6119721411675450087L;

	public ServerDirectoryImpl(IRI serverLocation) {
		super(serverLocation);
	}
	
	@Override
	public String toString() {
	    return "<Dir: " + getServerLocation() + ">";
	}

}
