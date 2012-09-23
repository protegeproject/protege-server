package org.protege.owl.server.core;

import org.protege.owl.server.api.RemoteServerDirectory;
import org.protege.owl.server.api.RemoteServerDocument;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerPath;
import org.semanticweb.owlapi.model.IRI;

public class ServerDirectoryImpl extends ServerDocumentImpl implements ServerDirectory {
	private static final long serialVersionUID = 6119721411675450087L;

	
    public RemoteServerDirectory createRemoteDocument(final String scheme, final String host, final int port) {
        return new RemoteServerDirectory() {
            @Override
            public IRI getServerLocation() {
                return getServerPath().getIRI(scheme, host, port);
            }
            
            @Override
            public Object getProperty(String key) {
                return ServerDirectoryImpl.this.getProperty(key);
            }
            
            @Override
            public ServerDirectory createServerDocument() {
                return ServerDirectoryImpl.this;
            }
            
            public String toString() {
                return "<Dir: " + getServerLocation() + ">";
            }
            
            @Override
            public int compareTo(RemoteServerDocument o) {
                return getServerLocation().compareTo(o.getServerLocation());
            }
        };
    }
    
	public ServerDirectoryImpl(ServerPath serverPath) {
		super(serverPath);
	}
	
	@Override
	public String toString() {
	    return "<Dir: " + getServerPath() + ">";
	}

}
