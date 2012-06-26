package org.protege.owl.server.impl;

import org.protege.owl.server.api.ServerDirectory;
import org.semanticweb.owlapi.model.IRI;

public class ServerDirectoryImpl extends ServerDocumentImpl implements ServerDirectory {
	
	public ServerDirectoryImpl(IRI serverLocation) {
		super(serverLocation);
	}

}
