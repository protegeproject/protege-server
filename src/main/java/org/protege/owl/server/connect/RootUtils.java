package org.protege.owl.server.connect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.protege.owl.server.api.RemoteServerDocument;
import org.protege.owl.server.api.ServerDocument;

public class RootUtils {
    private RootUtils() { };

    
    public static Collection<RemoteServerDocument> rootList(Collection<ServerDocument> docs, String scheme, String host, int port) {
        List<RemoteServerDocument> documents = new ArrayList<RemoteServerDocument>();
        for (ServerDocument document : docs) {
            documents.add(document.createRemoteDocument(scheme, host, port));
        }
        return documents;
    }

}
