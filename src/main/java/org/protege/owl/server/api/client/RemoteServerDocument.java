package org.protege.owl.server.api.client;

import org.protege.owl.server.api.server.ServerDocument;
import org.semanticweb.owlapi.model.IRI;

public interface RemoteServerDocument extends Comparable<RemoteServerDocument> {

    ServerDocument createServerDocument();

    IRI getServerLocation();

    Object getProperty(String key);
}
