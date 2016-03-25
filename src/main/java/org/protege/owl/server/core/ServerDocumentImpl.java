 package org.protege.owl.server.core;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.protege.owl.server.api.server.ServerPath;
import org.protege.owl.server.changes.api.ServerDocument;


public abstract class ServerDocumentImpl implements ServerDocument, Serializable {
	private static final long serialVersionUID = -3003767122936738208L;
	private ServerPath serverPath;
	private Map<String, Object> propertyMap;
	
	public ServerDocumentImpl(ServerPath serverPath) {
		this.serverPath = serverPath;
		propertyMap = new TreeMap<String, Object>();
	}

	@Override
	public ServerPath getServerPath() {
	    return serverPath;
	}
	
	public Object getProperty(String key) {
	    return propertyMap.get(key);
	}
	
	public void setProperty(String key, Object value) {
	    propertyMap.put(key, value);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ServerDocument)) {
			return false;
		}
		ServerDocument other = (ServerDocument) o;
		return serverPath.equals(other.getServerPath());
	}
	
	@Override
	public int hashCode() {
	    return serverPath.hashCode();
	}
	
	@Override
	public int compareTo(ServerDocument o) {
	    return serverPath.compareTo(o.getServerPath());
	}
}
