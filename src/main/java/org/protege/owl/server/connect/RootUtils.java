package org.protege.owl.server.connect;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.protege.owl.server.api.RemoteServerDocument;
import org.protege.owl.server.api.ServerDocument;
import org.semanticweb.owlapi.model.IRI;

public class RootUtils {
    private RootUtils() { };

    
    public static Collection<RemoteServerDocument> rootList(Collection<ServerDocument> docs, String scheme, String host, int port) {
        List<RemoteServerDocument> documents = new ArrayList<RemoteServerDocument>();
        for (ServerDocument document : docs) {
            documents.add(document.createRemoteDocument(scheme, host, port));
        }
        return documents;
    }
    
    public static IRI getRoot(IRI iri) throws URISyntaxException {
        URI uri = iri.toURI();
        return IRI.create(uri.getScheme() + "://" + uri.getAuthority());
    }

}
