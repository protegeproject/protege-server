package org.protege.owl.server.changes.api;

import org.semanticweb.owlapi.model.IRI;

public interface RemoteServerDocument extends Comparable<RemoteServerDocument> {

    ServerDocument createServerDocument();

    IRI getServerLocation();

    Object getProperty(String key);
}
