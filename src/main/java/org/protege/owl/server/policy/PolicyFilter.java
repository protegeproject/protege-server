package org.protege.owl.server.policy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.AuthToken;
import org.protege.owl.server.api.ServerDirectory;
import org.protege.owl.server.api.ServerDocument;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.api.ServerOntologyDocument;
import org.protege.owl.server.api.ServerPath;
import org.protege.owl.server.api.UserId;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.util.ServerFilterAdapter;

public class PolicyFilter extends ServerFilterAdapter {
    public static final String EXTENSION = "owner";
    
    private Logger logger = Logger.getLogger(PolicyFilter.class.getCanonicalName());

    public PolicyFilter(ServerFilter delegate) {
        super(delegate);
    }
    
    @Override
    public ServerDirectory createDirectory(AuthToken u, ServerPath serverPath) throws OWLServerException {
        ServerDirectory dir = super.createDirectory(u, serverPath);
        setOwner(u.getUserId(), dir);
        return dir;
    }
    
    @Override
    public ServerOntologyDocument createOntologyDocument(AuthToken u, ServerPath serverPath, Map<String, Object> settings) throws OWLServerException {
        ServerOntologyDocument doc =  super.createOntologyDocument(u, serverPath, settings);
        setOwner(u.getUserId(), doc);
        return doc;
    }
    
    private void setOwner(UserId u, ServerDocument doc) {
        try {
            String username = u.getUserName();
            OutputStreamWriter out = new OutputStreamWriter(getConfigurationOutputStream(doc, EXTENSION));
            out.write(username, 0, username.length());
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Could not set owner for server document, " + doc, e);
        }
    }
    
    private UserId getOwner(ServerDocument doc) {
        BufferedReader reader = null;
        try {
             reader = new BufferedReader(new InputStreamReader(getConfigurationInputStream(doc, EXTENSION)));
             return new UserId(reader.readLine());
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Could not read owner for server document, " + doc, e);
            return null;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Could not close open stream", e);
                }
            }
        }
    }
}
