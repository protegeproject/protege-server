package org.protege.owl.server.api_new.configuration;

import org.protege.owl.server.api_new.operation.Operation;

import java.util.Set;

/**
 * Manager for server-side configurations
 *
 * @author Rafael Gonçalves
 * Stanford Center for Biomedical Informatics Research
 */
public interface ServerConfigurationManager extends ConfigurationManager {

    Set<Operation> getAllowedOperations();


}
