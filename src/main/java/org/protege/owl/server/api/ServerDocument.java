package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.IRI;

public interface ServerDocument extends Comparable<ServerDocument> {
	IRI getServerLocation();
}
