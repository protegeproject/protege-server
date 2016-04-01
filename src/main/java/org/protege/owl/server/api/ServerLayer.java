package org.protege.owl.server.api;

import org.protege.owl.server.api.exception.OWLServerException;

import edu.stanford.protege.metaproject.Manager;
import edu.stanford.protege.metaproject.api.ClientConfiguration;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.UserId;
import edu.stanford.protege.metaproject.api.exception.MetaprojectNotLoadedException;
import edu.stanford.protege.metaproject.api.exception.ServerConfigurationNotLoadedException;

public abstract class ServerLayer implements Server {

    /**
     * Get the server configuration
     *
     * @return Server configuration
     */
    protected abstract ServerConfiguration getConfiguration();

    public ClientConfiguration getClientConfiguration(UserId userId) throws OWLServerException {
        try {
            return Manager.getConfigurationManager().getClientConfiguration(userId);
        }
        catch (MetaprojectNotLoadedException e) {
            throw new OWLServerException(e);
        }
        catch (ServerConfigurationNotLoadedException e) {
            throw new OWLServerException(e);
        }
    }
}
