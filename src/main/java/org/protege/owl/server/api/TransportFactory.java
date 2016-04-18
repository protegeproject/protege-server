package org.protege.owl.server.api;

import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.api.server.TransportHandler;

import edu.stanford.protege.metaproject.api.ServerConfiguration;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public interface TransportFactory {

    String getFactoryName();

    TransportHandler build(ServerConfiguration configuration) throws OWLServerException;
}
