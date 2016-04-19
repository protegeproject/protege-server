package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.RevisionPointer;

import java.io.Serializable;

public final class DocumentRevision implements Comparable<DocumentRevision>, Serializable {

    private static final long serialVersionUID = 7037205560605439026L;

    public static final DocumentRevision START_REVISION = new DocumentRevision(0);

    private int revision;

    public DocumentRevision(int revision) {
        this.revision = revision;
    }

    public int getRevisionNumber() {
        return revision;
    }

    public RevisionPointer asPointer() {
        return new RevisionPointer(this);
    }

    public int getRevisionDifferenceFrom(DocumentRevision start) {
        return revision - start.revision;
    }

    public DocumentRevision next() {
        return new DocumentRevision(revision + 1);
    }

    public DocumentRevision add(int delta) {
        return new DocumentRevision(revision + delta);
    }

    @Override
    public int compareTo(DocumentRevision o) {
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
        if (!(other instanceof DocumentRevision)) {
            return false;
        }
        return revision == ((DocumentRevision) other).revision;
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
