package org.protege.owl.server.api.server;

import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.changes.api.DocumentFactory;
import org.protege.owl.server.changes.api.ServerDocument;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

@Deprecated
public interface ServerInternals {
    /* Interfaces that are not visible to the client. */
    
   DocumentFactory getDocumentFactory();
    
   InputStream getConfigurationInputStream(String fileName) throws OWLServerException;
   
   OutputStream getConfigurationOutputStream(String fileName) throws OWLServerException;
        
   InputStream getConfigurationInputStream(ServerDocument doc, String extension) throws OWLServerException;
   
   OutputStream getConfigurationOutputStream(ServerDocument doc, String extension) throws OWLServerException;
   
   /**
    * Inform the server and its filters about the transports that are being used.
    * <p>
    * ServerFilters can use this to add their own functionality to the transport mechanism and 
    * to determine that the initialization sequence has been completed.
    * This is used in conjuction with the ServerTransport.start function.  The right sequence is
    * <pre>
    *        transport.start(server);
    *        server.setTransports(transports);
    * </pre>
    *  
    * @param transports transports
    */
   void setTransports(Collection<ServerTransport> transports);
   
   Collection<ServerTransport> getTransports();
   
   void addServerListener(ServerListener listener);
   
   void removeServerListener(ServerListener listener);
   
   void shutdown();
}
