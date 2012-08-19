package org.protege.owl.server.api;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.IRI;

public final class ServerPath implements Comparable<ServerPath>, Serializable {
    private static final long serialVersionUID = -9220695890106738528L;
    private List<String> components;
    
    public ServerPath(List<String> components) {
        this.components = new ArrayList<String>(components);
    }
    
    public ServerPath(IRI iri) {
        this(iri.toURI());
    }
    
    public ServerPath(URI uri) {
        String uriPath = uri.getPath();
        components = new ArrayList<String>();
        for (String component : uriPath.split("/")) {
            components.add(component);
        }
    }
    
    public List<String> getComponents() {
        return components;
    }
    
    public String pathAsString() {
        StringBuffer sb = new StringBuffer();
        boolean firstTime = true;
        for (String component : components) {
            if (firstTime) {
                firstTime = false;
            }
            else {
                sb.append("/");
            }
            sb.append(component);
        }
        return sb.toString();
    }
    
    public IRI getIRI(String scheme, String host, int port) {
        StringBuffer sb = new StringBuffer(scheme);
        sb.append("://");
        sb.append(host);
        if (port > 0) {
            sb.append(":");
            sb.append(port);
        }
        sb.append("/");
        sb.append(pathAsString());
        return IRI.create(sb.toString());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ServerPath)) {
            return false;
        }
        ServerPath other = (ServerPath) obj;
        return components.equals(other.components);
    }
    
    
    @Override
    public int hashCode() {
        return 42 * components.hashCode();
    }
    
    @Override
    public int compareTo(ServerPath o) {
        return pathAsString().compareTo(o.pathAsString());
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("[Path: ");
        sb.append(pathAsString());
        sb.append("]");
        return sb.toString();
    }
}
