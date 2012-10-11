package org.protege.owl.server.api;


import java.io.IOException;
import java.util.Set;

import org.protege.owl.server.api.exception.OWLServerException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;


/**
 * This interface describes factories that can transform an ontology or a server IRI and will return a 
 * Client that can talk to the server for the ontology or the server represented by the IRI.
 * <p/>
 * There are three issues that implementations of this interface can hide.  First there is the question of what types of IRI's are associated with
 * which clients and servers.  For instance, an IRI such as "rmi-owl2-server://wormhole.stanford.edu:4875" is associated with an RMI client.  A developer could write 
 * code that is aware of this fact and knows how to initialize and use the RMIClient class but this type of code is fragile and clumsy because it will not keep up with new 
 * transport protocols.  A factory implementing this class can report on whether it can support a particular type of IRI and if it can handle the IRI it can handle the details of
 * creating a client for that IRI.  In addition this interface enables the ClientRegistry which collects client factories and will use the client that is appropriate for a given 
 * transport.
 * <p/>
 * Second there is the problem of associating ontologies with their appropriate server and ontology document revision.  When an ontology is first downloaded from a server, the java code
 * that made the download can keep track of the server from whence the ontology came and the appropriate server document revision.  However if this information is to survive between jvm sessions
 * then we will need a way to persistantly store and retrieve this information.  One approach, which is what is currently implemented here, is ensure that when an ontology is saved to disk, this metadata
 * is stored in some nearby place where it can be retrieved when the ontology is reloaded.  In the future there may be variations of the current method and implementations of this interface
 * will hide and handle the details of that implementation.
 * <p/>
 * Finally there is the problem of authenticating to the server.  In many but not all implementations this will involve a user interaction and depending on the context this interaction
 * may involve a gui or it may be done from the command line.  This factory takes care of the authentication so that the caller does not have to concern itself with this detail.
 * An implementation may also cache and maintain authentication tokens so that the caller can get the same client in different contexts without having to worry about storing live clients.
 * This cache is very important to the Protege implementation which uses it to interact with the server in on on demand style.
 * 
 * 
 * metadata, iri format, authentication
 * @author tredmond
 *
 */
public interface ClientFactory {
    
    /**
     * Look at the storage location of the ontology to determine if there is metadata associated with this ontology indicating
     * the appropriate server ontology and server ontology revision for this ontology.
     * 
     * @param ontology
     * @return
     * @throws IOException
     */
    boolean hasSuitableMetaData(OWLOntology ontology) throws IOException;
    
    /**
     * If there is metadata associated with this ontology indicating a server side document and a ontology document revision
     * then this call returns the VersionedOntologyDocument that contains the server document information.
     * <p/>
     * This call should be called after checking for appropriate server metadata associated with this ontology with the hasSuitableMetaData() call.
     * 
     * @param ontology
     * @return
     * @throws IOException
     */
    VersionedOntologyDocument getVersionedOntologyDocument(OWLOntology ontology) throws IOException;
    
    /**
     * If an ontology has metadata indicating that it is associated with a server ontology then this method will create a client to connect to that server.
     * <p/>
     * This call may result in some factory specific interaction with a user to authenticate to the server.
     * 
     * @param ontology
     * @return
     * @throws OWLServerException
     * @throws IOException
     */
    Client connectToServer(OWLOntology ontology) throws OWLServerException, IOException;
    
    /**
     * Perform a simple syntactic check to see if this is the appropriate factory for this type of server IRI.
     * <p/>
     * This check will probably usually just be a check of the schema of the IRI.
     * 
     * @param serverLocation
     * @return
     */
    boolean isSuitable(IRI serverLocation);
    
    /**
     * Attempt to connect to a server based on an IRI.
     * <p/>
     * This should not be attempted if the isSuitable() call fails.
     * 
     * @param serverLocation
     * @return
     * @throws OWLServerException
     */
    Client connectToServer(IRI serverLocation) throws OWLServerException;
    
    /**
     * This does a quick check to see if it looks like a client can be provided quickly without, in particular,
     * any authentication step.  It is possible to pass this check and still fail to quickly connect to the server.
     * @param serverLocation
     * @return
     */
    boolean hasReadyConnection(IRI serverLocation);
    
    /**
     * This returns a set of server IRI's that appear to be ready to use.  In particular at some point in the past, the authentication
     * check passed and there is an authentication token on the factory ready to use.
     * @return
     */
    Set<IRI> getReadyConnections();
    
    /**
     * This tries to use existing information to connect to a server based on an IRI.  It may fail and return null if, for instance,
     * the cached authentication token has expired.
     * @param serverLocation
     * @return A Client object corresponding to the serverLocation.  It can be null if the call failed.
     */
    Client quickConnectToServer(IRI serverLocation);

}
