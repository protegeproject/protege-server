package org.protege.owl.server.api_new.configuration;

/**
 * Manager for client-side configurations
 *
 * @author Rafael Gonçalves <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface ClientConfigurationManager extends ConfigurationManager {

    /**
     * Get the time (in seconds) between client-server synchronisation attempts
     *
     * @return Synchronisation delay in seconds
     */
    int getSynchronisationDelay();

    /**
     * Set the synchronisation delay time
     *
     * @param synchronisationDelay  Time in seconds
     */
    void setSynchronisationDelay(int synchronisationDelay);

}
