package org.protege.owl.server.core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerOntologyDocument;
import org.protege.owl.server.util.ServerFilterAdapter;

public class SynchronizationFilter extends ServerFilterAdapter {
    private Map<ServerOntologyDocument, Integer> readerMap = new TreeMap<ServerOntologyDocument, Integer>();
    private Set<ServerOntologyDocument> writers = new TreeSet<ServerOntologyDocument>();
    
    public SynchronizationFilter(Server delegate) {
        super(delegate);
    }

}
