package org.protege.owl.server.impl;

import java.io.File;

import org.protege.owl.server.api.OntologyDocument;
import org.protege.owl.server.api.ServerDirectory;
import org.semanticweb.owlapi.model.IRI;

public class ServerDirectoryImpl implements ServerDirectory {
	private File dir;
	
	public ServerDirectoryImpl(File dir) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Not a directory");
		}
		this.dir = dir;
	}

	@Override
	public IRI getLocation() {
		return IRI.create(dir);
	}

	@Override
	public boolean isOntologyDocument() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OntologyDocument asOntologyDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDirectory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ServerDirectory asServerDirectory() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
