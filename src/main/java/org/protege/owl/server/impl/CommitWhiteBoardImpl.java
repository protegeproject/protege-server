package org.protege.owl.server.impl;

import java.io.IOException;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.CommitWhiteBoard;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.Server;

public class CommitWhiteBoardImpl implements CommitWhiteBoard {
	private Server server;
	private RemoteOntologyDocument workingDocument;
	private ChangeDocument         workingClientSideChanges;
	private ChangeDocument         fullServerSideHistory;
	private ChangeDocument         serverSideChangesSinceClientCommit;
	
	

	public CommitWhiteBoardImpl(Server server) {
		this.server = server;
	}

	@Override
	public void init(RemoteOntologyDocument doc, ChangeMetaData metaData,
					  ChangeDocument changes) {
		if (!doc.equals(workingDocument) || !changes.equals(workingClientSideChanges)) {
			workingDocument = doc;
			workingClientSideChanges = changes;
			fullServerSideHistory = null;
			serverSideChangesSinceClientCommit = null;
		}
	}

	@Override
	public ChangeDocument getFullChanges() throws IOException {
		if (fullServerSideHistory == null) {
			fullServerSideHistory = server.getChanges(null, workingDocument, OntologyDocumentRevision.START_REVISION, null);
		}
		return fullServerSideHistory;
	}

	@Override
	public ChangeDocument getServerChangesSinceCommit() throws IOException {
		if (serverSideChangesSinceClientCommit == null) {
			OntologyDocumentRevision clientSideRevision = workingClientSideChanges.getStartRevision();
			serverSideChangesSinceClientCommit = getFullChanges().cropChanges(clientSideRevision, null);
		}
		return serverSideChangesSinceClientCommit;
	}

}
