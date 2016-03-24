package org.protege.owl.server.api;

import java.io.Serializable;

public final class OntologyDocumentRevision implements Comparable<OntologyDocumentRevision>, Serializable {
	private static final long serialVersionUID = 7037205560605439026L;

	public static final OntologyDocumentRevision START_REVISION = new OntologyDocumentRevision(0);
	
	private int revision;
	
	public OntologyDocumentRevision(int revision) {
		this.revision = revision;
	}
	
	public RevisionPointer asPointer() {
	    return new RevisionPointer(this);
	}

	public int getRevisionDifferenceFrom(OntologyDocumentRevision start) {
	    return revision - start.revision;
	}
	
	public OntologyDocumentRevision next() {
		return new OntologyDocumentRevision(revision + 1);
	}
	
	public OntologyDocumentRevision add(int delta) {
	    return new OntologyDocumentRevision(revision + delta);
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
		return revision == ((OntologyDocumentRevision) other).revision;
	}
	
	@Override
	public int hashCode() {
		return revision + 42;
	}
	
	@Override
	public String toString() {
		return "" + revision;
	}

}
