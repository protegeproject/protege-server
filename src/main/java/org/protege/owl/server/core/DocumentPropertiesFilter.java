package org.protege.owl.server.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.UserId;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.Server;
import org.protege.owl.server.api.server.ServerFilter;
import org.protege.owl.server.api.server.ServerPath;
import org.protege.owl.server.changes.api.ServerDirectory;
import org.protege.owl.server.changes.api.ServerDocument;
import org.protege.owl.server.changes.api.ServerOntologyDocument;
import org.protege.owl.server.changes.api.SingletonChangeHistory;
import org.protege.owl.server.util.ServerFilterAdapter;

@Deprecated
public class DocumentPropertiesFilter extends ServerFilterAdapter {
    public static final String OWNER         = "owner";
    public static final String DATE_CREATED  = "creation";
    public static final String DATE_MODIFIED = "modified";
    public static final String EXTENSION     = "properties";
    
    private Logger logger = LoggerFactory.getLogger(DocumentPropertiesFilter.class.getCanonicalName());
    
    public static Properties readProperties(Server server, ServerDocument doc) throws IOException, OWLServerException {
        Properties p = new Properties();
        p.load(server.getConfigurationInputStream(doc, EXTENSION));
        return p;
    }
    
    public DocumentPropertiesFilter(ServerFilter delegate) {
        super(delegate);
    }
    
    @Override
    public ServerDirectory createDirectory(AuthToken u, ServerPath serverPath) throws OWLServerException {
        ServerDirectory dir = super.createDirectory(u, serverPath);
        saveCreatedProperties(dir, u.getUserId());
        return dir;
    }
    
    
    @Override
    public ServerOntologyDocument createOntologyDocument(AuthToken u, ServerPath serverPath, Map<String, Object> settings) throws OWLServerException {
        ServerOntologyDocument doc = super.createOntologyDocument(u, serverPath, settings);
        saveCreatedProperties(doc, u.getUserId());
        return doc;
    }
    

    private void saveCreatedProperties(ServerDocument doc, UserId u) {
        try {
            Properties p = new Properties();
            p.put(OWNER, u.getUserName());
            doc.setProperty(OWNER, u);
            Long now = System.currentTimeMillis();
            p.put(DATE_CREATED, now.toString());
            p.put(DATE_MODIFIED, now.toString());
            p.store(getConfigurationOutputStream(doc, EXTENSION), "Automatically generated document properties");
            updateDocumentProperties(doc, p);
        }
        catch (Exception e) {
            // what can you do?
            logger.warn("Could not associate standard document properties with document " + doc, e);
        }
    }
    
    @Override
    public void commit(AuthToken u, ServerOntologyDocument doc, SingletonChangeHistory changes) throws OWLServerException {
        super.commit(u, doc, changes);
        saveModifiedProperties(doc, u.getUserId());
    }

    private void saveModifiedProperties(ServerOntologyDocument doc, UserId u) {
        try {
            Properties p = readProperties(this, doc);
            Long now = System.currentTimeMillis();
            p.put(DATE_MODIFIED, now.toString());
            p.store(getConfigurationOutputStream(doc, EXTENSION), "Updated by Document Properties Plugin");
        }
        catch (Exception e) {
            logger.warn("Could not update modification date in properties for document", e);
        }
    }
    
    @Override
    public ServerDocument getServerDocument(AuthToken u, ServerPath servePath) throws OWLServerException {
        ServerDocument doc = super.getServerDocument(u, servePath);
        updateDocumentProperties(doc);
        return doc;
    }
    
    @Override
    public Collection<ServerDocument> list(AuthToken u, ServerDirectory dir) throws OWLServerException {
        Collection<ServerDocument> docs = super.list(u, dir);
        for (ServerDocument doc : docs) {
            updateDocumentProperties(doc);
        }
        return docs;
    }
    
    private void updateDocumentProperties(ServerDocument doc) {
        try {
            Properties p = readProperties(this, doc);
            updateDocumentProperties(doc, p);
        } catch (Exception e) {
            logger.warn("could not update document properties", e);
        }
    }
    
    private void updateDocumentProperties(ServerDocument doc, Properties p) {
        doc.setProperty(OWNER, p.get(OWNER));
        doc.setProperty(DATE_CREATED, p.get(DATE_CREATED));
        doc.setProperty(DATE_MODIFIED, p.get(DATE_MODIFIED));
    }
}