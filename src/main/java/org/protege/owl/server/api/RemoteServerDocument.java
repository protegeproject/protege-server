package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.IRI;

public interface RemoteServerDocument extends Comparable<RemoteServerDocument> {
    ServerDocument createServerDocument();
    
    IRI getServerLocation();
    
    Object getProperty(String key);
}
