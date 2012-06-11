package org.protege.owl.server.api;

import java.io.Serializable;

public class OntologyDocumentRevision implements Comparable<OntologyDocumentRevision>, Serializable {
	private static final long serialVersionUID = 7037205560605439026L;

	public static final OntologyDocumentRevision START_REVISION = new OntologyDocumentRevision(0);
	
	private int revision;
	
	public OntologyDocumentRevision(int revision) {
		this.revision = revision;
	}
	
	public int getRevision() {
		return revision;
	}
	
	@Override
	public int compareTo(OntologyDocumentRevision o) {
		if (revision > o.revision) {
			return 1;
		}
		else if (revision < o.revision) {
			return -1;
		}
		else {
			return 0;
		}
	}

}
