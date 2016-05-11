package org.protege.editor.owl.server.versioning;

import java.io.Serializable;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class DocumentRevision implements Comparable<DocumentRevision>, Serializable {

    private static final long serialVersionUID = 7037205560605439026L;

    public static final DocumentRevision START_REVISION = DocumentRevision.create(0);

    private int revision;

    private DocumentRevision(int revision) {
        this.revision = revision;
    }

    /**
     * Creates a document revision given its revision number.
     *
     * @param revision
     *          The revision number
     * @return an instance of {@code DocumentRevision}.
     */
    public static DocumentRevision create(int revision) {
        return new DocumentRevision(revision);
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
