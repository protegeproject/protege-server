package org.protege.owl.server.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.protege.owl.server.api.exception.OWLServerException;

public interface ServerInternals {
    /* Interfaces that are not visible to the client. */
    
   DocumentFactory getDocumentFactory();
    
   InputStream getConfigurationInputStream(String fileName) throws OWLServerException;
   
   OutputStream getConfigurationOutputStream(String fileName) throws OWLServerException;
        
   InputStream getConfigurationInputStream(ServerDocument doc, String extension) throws OWLServerException;
   
   OutputStream getConfigurationOutputStream(ServerDocument doc, String extension) throws OWLServerException;
   
   void setTransports(Collection<ServerTransport> transports);
   
   Collection<ServerTransport> getTransports();
   
   void addServerListener(ServerListener listener);
   
   void removeServerListener(ServerListener listener);
   
   void shutdown();
}
