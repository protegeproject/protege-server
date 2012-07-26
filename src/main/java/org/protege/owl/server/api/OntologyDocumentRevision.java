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
	
	public OntologyDocumentRevision next() {
		return new OntologyDocumentRevision(revision + 1);
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
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof OntologyDocumentRevision)) {
			return false;
		}
		return revision == ((OntologyDocumentRevision) other).getRevision();
	}
	
	@Override
	public int hashCode() {
		return revision + 42;
	}
	
	@Override
	public String toString() {
		return "<Revision " + revision + ">";
	}

}
