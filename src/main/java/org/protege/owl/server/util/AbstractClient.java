package org.protege.owl.server.util;

import java.net.URI;

import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.semanticweb.owlapi.model.IRI;

public abstract class AbstractClient implements Client {

	
	@Override
	public final IRI getServerIRI() {
		return IRI.create(getScheme() + "//" + getAuthority());
	}

	
	@Override
	public boolean isCompatible(VersionedOWLOntology versionedOntology) {
		URI serverUri = versionedOntology.getServerDocument().getServerLocation().toURI();
		if (serverUri == null) {
			return false;
		}
		return serverUri.getScheme().equals(getScheme()) && serverUri.getAuthority().equals(getAuthority());
	}
}
