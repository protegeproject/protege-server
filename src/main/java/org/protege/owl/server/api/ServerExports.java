package org.protege.owl.server.api;

import java.util.Collection;
import java.util.Map;

import org.protege.owl.server.api.exception.OWLServerException;

public interface ServerExports {
    ServerDocument getServerDocument(AuthToken u, ServerPath serverIRI) throws OWLServerException;
    
    Collection<ServerDocument> list(AuthToken u, ServerDirectory dir) throws OWLServerException;
        
    ServerDirectory createDirectory(AuthToken u, ServerPath serverIRI) throws OWLServerException;

    ServerOntologyDocument createOntologyDocument(AuthToken u, ServerPath serverIRI, Map<String, Object> settings) throws OWLServerException;
        
    ChangeHistory getChanges(AuthToken u, ServerOntologyDocument doc, OntologyDocumentRevision start, OntologyDocumentRevision end) throws OWLServerException;

    /**
     * The call to commit changes.
     * <p/>
     * The server is free to compress the changes in the ChangeDocument and only the first ChangeMetaData is guaranteed to survive.
     * 
     * @param u
     * @param doc
     * @param changes
     * @param myCommits
     * @param option
     * @return
     * @throws OWLServerException
     */
    void commit(AuthToken u, ServerOntologyDocument doc, ChangeHistory changes) throws OWLServerException;

}
