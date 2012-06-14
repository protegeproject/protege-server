package org.protege.owl.server.impl;

import java.io.File;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.semanticweb.owlapi.model.IRI;

public class VersionedOntologyDocumentImpl implements VersionedOntologyDocument {
	private DocumentFactory factory;
	private File localAddress;
	private File changesDoc;
	
	public VersionedOntologyDocumentImpl(DocumentFactory factory, IRI localAddress, IRI backingStore) {
		
	}

	@Override
	public IRI getLocalAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChangeDocument getLocalHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addToLocalHistory(ChangeDocument changes) {
		// TODO Auto-generated method stub

	}

	@Override
	public RemoteOntologyDocument getServerDocument() {
		// TODO Auto-generated method stub
		return null;
	}

}
