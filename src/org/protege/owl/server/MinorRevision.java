package org.protege.owl.server;

public interface MinorRevision extends Comparable<MinorRevision> {
    MinorRevision getHead();
    MinorRevision extend();
}
